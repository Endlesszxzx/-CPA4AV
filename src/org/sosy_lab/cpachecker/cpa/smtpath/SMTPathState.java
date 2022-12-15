// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2022 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.cpa.smtpath;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormula;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.java_smt.api.BooleanFormula;

public class SMTPathState implements AbstractState {

  private transient FormulaManagerView fmgr;
  private final PathFormula formula;

  public SMTPathState(
      FormulaManagerView pFmgr,
      PathFormula pFormula) {
    this.fmgr = checkNotNull(pFmgr);
    this.formula = checkNotNull(pFormula);
  }

  public PathFormula asFormula() {
    return formula;
  }

  public BooleanFormula asBooleanFormula() {
    return formula.getFormula();
  }

  public boolean isTrue() {
    return fmgr.getBooleanFormulaManager().isTrue(formula.getFormula());
  }

  public boolean isFalse() {
    return fmgr.getBooleanFormulaManager().isFalse(formula.getFormula());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 7;
    result = prime * result + Objects.hashCode(formula);
    return result;
  }

  @Override
  public boolean equals(Object pObj) {
    if ((pObj == null) || !(pObj instanceof SMTPathState)) {
      return false;
    }
    SMTPathState otherState = (SMTPathState) pObj;

    return formula.equals(otherState.formula);
  }

  @Override
  public String toString() {
    // we print the formula only when it is small
    String abs = "";
    if (isTrue()) {
      abs = ": true";
    } else if (isFalse()) {
      abs = ": false";
    }
    return "Formula" + abs;
  }

}
