package org.sosy_lab.cpachecker.cpa.dataaccess;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.cpachecker.core.CPAcheckerResult;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.util.statistics.ThreadSafeTimerContainer;

import java.io.PrintStream;

public class DataAccessStatistics implements Statistics {

    final ThreadSafeTimerContainer transferTimer = new ThreadSafeTimerContainer("time for DataAccessTransferRelation");

    @Override
    public void printStatistics(PrintStream out, CPAcheckerResult.Result result, UnmodifiableReachedSet reached) {
        System.out.println("\u001b[32mtime for TransferRelation:\u001b[0m      \u001b[33m" + transferTimer + "\u001b[0m");
    }

    @Override
    public @Nullable String getName() {
        return "DataAccessCPA";
    }
}
