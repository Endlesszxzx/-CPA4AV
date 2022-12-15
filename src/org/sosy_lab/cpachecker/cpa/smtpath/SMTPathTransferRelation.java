// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2022 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.cpa.smtpath;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.FunctionCallEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.core.defaults.SingleEdgeTransferRelation;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.cpa.threading.ThreadingState;
import org.sosy_lab.cpachecker.cpa.threadingintp.ThreadingIntpState;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormula;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManager;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.Solver;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.SolverException;

@Options(prefix = "cpa.smtpath")
public class SMTPathTransferRelation extends SingleEdgeTransferRelation {

  private final LogManager logger;

  private final Solver solver;
  private final PathFormulaManager pfmgr;
  private final FormulaManagerView fmgr;
  private final BooleanFormulaManager bfmgr;

  @Option(
    secure = true,
    description = "Wether the satisfiability of a path formula is needed to "
        + "be checked when a succeccsor is generated.")
  private boolean unsatCheck = true;

  @Option(
    secure = true,
    description = "Wether the input program is a concurrnt program. If yes, "
        + "the ThreadingCPA should be used.")
  private boolean forConcurrentPrograms = false;

  public SMTPathTransferRelation(
      Configuration pConfig,
      LogManager pLogger,
      Solver pSolver,
      PathFormulaManager pPfmgr,
      FormulaManagerView pFmgr) {
    try {
      pConfig.inject(this);
    } catch (InvalidConfigurationException e) {
      pLogger.log(Level.SEVERE, "Invalid configuration for SMTPathCPA: " + e);
    }

    this.logger = checkNotNull(pLogger);

    this.solver = checkNotNull(pSolver);
    this.pfmgr = checkNotNull(pPfmgr);
    this.fmgr = checkNotNull(pFmgr);
    this.bfmgr = this.fmgr.getBooleanFormulaManager();
  }

  @Override
  public Collection<? extends AbstractState>
      getAbstractSuccessorsForEdge(AbstractState pState, Precision pPrecision, CFAEdge pCfaEdge)
          throws CPATransferException, InterruptedException {
    SMTPathState element = (SMTPathState) pState;

    // check whether the current path formula is already false.
    if (element.isFalse()) {
      return ImmutableSet.of();
    }

    //// short cut.
    // if the formula of current edge is 'true' or 'false', we actually need not
    // to calculate the strongest post.
    PathFormula curEdgeFormula =
        pfmgr.makeAnd(pfmgr.makeEmptyPathFormula(element.asFormula()), pCfaEdge);

    if (bfmgr.isTrue(curEdgeFormula.getFormula())) {
      // for this case, we only need to preserve the original path formula.
      return Collections.singleton(new SMTPathState(fmgr, element.asFormula()));
    } else if (bfmgr.isFalse(curEdgeFormula.getFormula())) {
      // this case seems never occur.
      return ImmutableSet.of();
    } else {
      try {
        // we only need to perform the satisfiability checking when an assume edge is reached.
        if (unsatCheck && (pCfaEdge instanceof CAssumeEdge)) {
          // check whether the current assume edge is conflict with the original path formula.
          boolean unsat =
              solver.isUnsat(
                  fmgr.makeAnd(element.asFormula().getFormula(), curEdgeFormula.getFormula()));

          if (unsat) {
            logger.log(Level.FINEST, "Current PathFormula is unsatisfiable.");
            return ImmutableSet.of();
          }

          // we need not to conjunct the assume edge with the original path formula.
          return Collections.singleton(new SMTPathState(fmgr, element.asFormula()));
        }

        // for other type of edges, we need to calculate the strongest post.
        PathFormula pathFormula = pfmgr.makeAnd(element.asFormula(), pCfaEdge);
        return Collections.singleton(new SMTPathState(fmgr, pathFormula));
      } catch (SolverException e) {
        throw new CPATransferException("Solver failed during successor generation", e);
      }
    }
  }

  /**
   * Create a formula for the thread creation.
   *
   * <p>
   * For `pthread_create(t, ?, foo, arg), we create a formula for the dummy edge `foo(arg)` and
   * conjunct it with the original path formula.
   *
   * @implNote Due to the ThreadingCPA will delete the temporary information of entry, SMTPathCPA
   *           should be ordered before the ThreadingCPA!!
   */
  @Override
  public Collection<? extends AbstractState> strengthen(
      AbstractState pState,
      Iterable<AbstractState> pOtherStates,
      @Nullable CFAEdge pCfaEdge,
      Precision pPrecision)
      throws CPATransferException, InterruptedException {
    if (forConcurrentPrograms) {
      boolean threadingStateFound = false;
      for (AbstractState lElement : pOtherStates) {
        if (lElement instanceof ThreadingIntpState) {
          threadingStateFound = true;
          ThreadingIntpState threadingElement = (ThreadingIntpState) lElement;
          FunctionCallEdge threadEntryFunc = threadingElement.getEntryFunction();

          if (threadEntryFunc != null) {
            // current edge should be an pthread_create edge, a formula for the pthread
            // creation arguments should be generated.
            SMTPathState smtpathElement = (SMTPathState) pState;

            //// short cut.
            // if the formula of current edge is 'true' or 'false', we actually need not
            // to calculate the strongest post.
            BooleanFormula entryEdgeBooleanFormula =
                pfmgr
                    .makeAnd(
                        pfmgr.makeEmptyPathFormula(smtpathElement.asFormula()),
                        threadEntryFunc)
                    .getFormula();

            if (bfmgr.isTrue(entryEdgeBooleanFormula)) {
              // for this case, we only need to preserve the original path formula.
              return Collections.singleton(new SMTPathState(fmgr, smtpathElement.asFormula()));
            } else if (bfmgr.isFalse(entryEdgeBooleanFormula)) {
              // this case seems never occur.
              return ImmutableSet.of();
            } else {
              // calculate the strongest post.
              PathFormula pathFormula =
                  pfmgr.makeAnd(smtpathElement.asFormula(), entryEdgeBooleanFormula);

              SMTPathState successor = new SMTPathState(fmgr, pathFormula);
              return Collections.singleton(successor);
            }
          }
        }
      }

      if (!threadingStateFound) {
        logger.log(
            Level.SEVERE,
            "Concurrent program is given, but no ThreadingState can be found. "
                + "Please add ThreadingCPA.");
      }

      return Collections.singleton(pState);
    } else {
      // we need not to strength the serial program.
      return Collections.singleton(pState);
    }
  }

}
