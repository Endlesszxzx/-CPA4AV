package org.sosy_lab.cpachecker.cpa.threadingintp;

import com.google.common.base.Function;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustment;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustmentResult;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.dependence.conditional.EdgeVtx;

import java.util.*;

import static com.google.common.collect.FluentIterable.from;

public class ThreadingIntpPrecisionAdjustment implements PrecisionAdjustment {

    private Map<CFAEdge, ThreadingIntpState> optimizingStrategy = new HashMap<>();

    public ThreadingIntpPrecisionAdjustment(Map<CFAEdge, ThreadingIntpState> optimizingStrategy) {
        this.optimizingStrategy = optimizingStrategy;
    }

    public ThreadingIntpPrecisionAdjustment() {
    }


    public Map<CFAEdge, ThreadingIntpState> getOptimizingStrategy() {
        return optimizingStrategy;
    }

    public void setOptimizingStrategy(Map<CFAEdge, ThreadingIntpState> optimizingStrategy) {
        this.optimizingStrategy = optimizingStrategy;
    }

    private int times;
    private List<Boolean> flag = new ArrayList<>();


    @Override
    public Optional<PrecisionAdjustmentResult> prec(
            AbstractState pState,
            Precision pPrecision,
            UnmodifiableReachedSet pStates,
            Function<AbstractState, AbstractState> pStateProjection,
            AbstractState pFullState)
            throws CPAException, InterruptedException {

        System.out.println("Test");
        ThreadingIntpState threadingIntpState = (ThreadingIntpState) pState;

        // 得到 cfaEdge
        ARGState argCurrent = (ARGState) pFullState;
        ARGState argParent = null;
        CFAEdge curEdge = null;
        if (argCurrent.getParents() != null && !argCurrent.getParents().isEmpty()) {
            assert argCurrent.getParents().size() == 1;
            argParent = from(argCurrent.getParents()).toList().get(0);
            curEdge = argParent.getEdgeToChild(argCurrent);
        }

        // 获得当前节点所在函数
        String intpFunc = threadingIntpState.getActiveThread();
        if (!threadingIntpState.getIntpStack().isEmpty()) {
            intpFunc = threadingIntpState.getIntpStack().getLast();
        }

        // 先使用 hasISR 判断是否同时存在中断节点与非中断节点
        if (ThreadingIntpTransferRelation.getHasISR()) {
            if (ThreadingIntpTransferRelation.getPriorityMap().containsKey(intpFunc)) {
                return Optional.of(
                        PrecisionAdjustmentResult
                                .create(pState, pPrecision, PrecisionAdjustmentResult.Action.CONTINUE));
            } else {
                if (curEdge != null)
                    optimizingStrategy.put(curEdge, threadingIntpState);
            }
        }

        // 得到当前 threadingIntpState 的所在函数名，图二中，如果当前位于情况1的节点 c，则将 c 节点进行存储；其余情况都直接返回
        // 在 threadingIntpState 中，设置一个优化集合 OptimizingVarInISR 来存放图一中待优化的节点 c
        // OptimizingVarInISR 存放了，当前节点之前的所插入的中断，对共享节点的遍历信息
        // ① 如果 OptimizingVarInISR 节点为空，那么说明：当前并未进入中断，且没有对相应的中断变量进行操作
        // ② 获取当前 intpStack 栈顶的中断函数名，如果 OptimizingVarInISR 节点内，没有该函数，或者有该函数但没有对应的变量操作，那么说明，当前节点是中断节点或者
//        if (threadingIntpState.getOptimizingVarInISR() == null || threadingIntpState.getOptimizingVarInISR().isEmpty()) {
//            // 当前节点内的优化池中无中断，但 intpStack 不空，说明，当前节点是新开的
//            return Optional.of(
//                    PrecisionAdjustmentResult
//                            .create(pState, pPrecision, PrecisionAdjustmentResult.Action.CONTINUE));
//        }
//        String intpFunc = threadingIntpState.getActiveThread();
//        if (!threadingIntpState.getIntpStack().isEmpty()) {  // 首先检测 intpStack 中是否有中断
//
//            String intpFuncName = threadingIntpState.getIntpStack().getLast(); // 得到最新的一个中断函数
//            if (!threadingIntpState.getOptimizingVarInISR().containsKey(intpFuncName) || threadingIntpState.getOptimizingVarInISR().get(intpFuncName).isEmpty()) {
//                // 当优化策略存储集中没有最新的中断信息 或者 其对应的中断内的变量信息为空，就将当前 threadingIntpState 节点视为中断内的节点，返回
//                return Optional.of(
//                        PrecisionAdjustmentResult
//                                .create(pState, pPrecision, PrecisionAdjustmentResult.Action.CONTINUE));
//            }
//
//        }
//

        boolean canNotInsertISR = false;
        if (curEdge != null) {
            Set<String> noInsertISR = judgeCanInsertISR(threadingIntpState, curEdge);
            if (!noInsertISR.isEmpty() && noInsertISR.contains(intpFunc)) {
                return Optional.empty();
            }
        }

        return Optional.of(
                PrecisionAdjustmentResult
                        .create(pState, pPrecision, PrecisionAdjustmentResult.Action.CONTINUE));
    }

    private Set<String> judgeCanInsertISR(ThreadingIntpState threadingIntpState, CFAEdge curEdge) {
        // Copy the information of node
        String intpFunc = threadingIntpState.getActiveThread();
        Set<String> noInsertISR = new HashSet<>();     // 用于存放不用插入的中断函数
        if (!threadingIntpState.getIntpStack().isEmpty()) {
            intpFunc = threadingIntpState.getIntpStack().getLast();
        }
        if (!ThreadingIntpTransferRelation.getPriorityMap().containsKey(intpFunc)) {
            for (CFAEdge preCfaEdge : optimizingStrategy.keySet()) {  // 得到之前存储的边 edge1
                CFANode preSucNode = preCfaEdge.getSuccessor();    // 得到节点 c
                for (int i = 0; i < preSucNode.getNumLeavingEdges(); i++) {  //遍历节点 c 的出边，找到 d
                    CFAEdge preSucEdge = preSucNode.getLeavingEdge(i);

                    if (curEdge == preSucEdge) { // 判断当前边是否位于 edge3
                        // 将 d' 中得到的关于中断的信息交付给 d
                        optimizingStrategy.get(preCfaEdge).setOptimizingVarInISR(threadingIntpState.getOptimizingVarInISR());
                    }
                }
            }


            // Judge whether using this strategy
            EdgeVtx sucedgeInfo = (EdgeVtx) ThreadingIntpTransferRelation.getCondDepGraph().getDGNode(curEdge.hashCode());   // 得到 edge3 的边信息
            if (sucedgeInfo != null) {
                Set<String> edgeRWSharedVarSet = new HashSet<>(); // 得到边上都有哪些变量
                edgeRWSharedVarSet.addAll(from(sucedgeInfo.getgReadVars()).transform(v -> v.getName()).toSet());
                edgeRWSharedVarSet.addAll(from(sucedgeInfo.getgWriteVars()).transform(v -> v.getName()).toSet());
                for (String var : edgeRWSharedVarSet) {
                    Map<String, Set<String>> optimizingVarInISR = threadingIntpState.getOptimizingVarInISR();
                    for (String intpFuncName : optimizingVarInISR.keySet()) {
                        Set<String> intpNoUseingVarSet = optimizingVarInISR.get(intpFuncName);
                        if (ThreadingIntpTransferRelation.getIntpFuncRWSharedVarMap().get(intpFuncName).containsKey(var) && intpNoUseingVarSet.contains(var)) {
                            // 如果当前变量 var 在 ISR 中有，但实际并未使用，则将当前中断函数删除。
                            noInsertISR.add(intpFuncName);
                        }
                    }
                }
            }
        }
        return noInsertISR;
    }


}
