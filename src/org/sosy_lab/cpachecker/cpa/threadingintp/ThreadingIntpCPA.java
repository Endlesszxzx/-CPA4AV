// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.cpa.threadingintp;

import com.google.common.base.Preconditions;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.defaults.AbstractCPA;
import org.sosy_lab.cpachecker.core.defaults.AutomaticCPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.*;
import org.sosy_lab.cpachecker.cpa.cintp.CIntpPrecisionAdjustment;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.util.globalinfo.GlobalInfo;

import java.util.Optional;
import java.util.logging.Level;

@Options(prefix = "cpa.threadingintp")
public class ThreadingIntpCPA extends AbstractCPA {
    @Option(secure = true, description = "How many levels of interruption is support?")
    private int maxInterruptLevel = 3;

    @Option(secure = true, description = "Which order of interruption priority to use?")
    private ThreadingIntpTransferRelation.InterruptPriorityOrder intpPriOrder = ThreadingIntpTransferRelation.InterruptPriorityOrder.BH;


    public static CPAFactory factory() {
        // 函数制造工厂，必须实现，向 CPAchecker 注册
        return AutomaticCPAFactory.forType(ThreadingIntpCPA.class);
    }

    public ThreadingIntpCPA(Configuration config, LogManager pLogger, CFA pCfa) throws InvalidConfigurationException {
        // 构造函数 super 中前两个 sep 表示的是 merge( "sep" or "join" ) & stop( "join" )
        super("sep", "sep", new ThreadingIntpTransferRelation(config, pCfa, pLogger));
    }


    @Override
    public AbstractState getInitialState(CFANode pNode, StateSpacePartition pPartition) throws InterruptedException {
        Preconditions.checkNotNull(pNode);   // 是否为空 （先决条件）
        // 我们创建了一个空的 threadingState 并且将主函数作为第一个线程
        // 我们使用一个主函数的名字作为线程标识符
        String mainThread = pNode.getFunctionName();  // 得到函数名
        return ((ThreadingIntpTransferRelation) getTransferRelation()).addNewThread(
                new ThreadingIntpState(maxInterruptLevel),
                mainThread,
                intpPriOrder.equals(ThreadingIntpTransferRelation.InterruptPriorityOrder.BH)
                        ? ThreadingIntpState.MIN_PRIORITY_NUMBER
                        : ThreadingIntpState.MAX_PRIORITY_NUMBER,
                ThreadingIntpState.MIN_THREAD_NUM,
                mainThread);
    }

    @Override
    public PrecisionAdjustment getPrecisionAdjustment() {
        return new ThreadingIntpPrecisionAdjustment();
    }
}
