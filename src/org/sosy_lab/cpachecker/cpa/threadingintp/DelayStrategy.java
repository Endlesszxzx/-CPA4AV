package org.sosy_lab.cpachecker.cpa.threadingintp;

import org.checkerframework.checker.units.qual.C;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.util.dependence.conditional.ConditionalDepGraph;
import org.sosy_lab.cpachecker.util.dependence.conditional.EdgeVtx;
import org.sosy_lab.cpachecker.util.dependence.conditional.Var;

import java.util.*;

public class DelayStrategy {
    Map<String, CFAEdge> delayVar;    // varName -> edge

    public Map<String, CFAEdge> getDelayVar() {
        return delayVar;
    }


    public DelayStrategy() {
        delayVar = new HashMap<>();
    }

    public void setDelayVar(Set<String> gVars, CFAEdge edge) {
        for (String name : gVars) {
            if (delayVar.containsKey(name)) {
                delayVar.remove(name);
                delayVar.put(name, edge);
            } else {
                delayVar.put(name, edge);
            }
        }
    }


    public CFAEdge delayStrategy(Set<String> gVar, String callFunction, String disIntpFunc, String RorW, ConditionalDepGraph condDepGraph, CFAEdge cedge) {
        CFAEdge val = null;
        // 如果之前有 读/写，或有 disable，则进行中断的插入
        // 否则不进行中断插入
        for (String var : gVar) {
            if (delayVar.containsKey(var)) {
                if (RorW == "R") {
                    CFAEdge edge = delayVar.get(var);
                    EdgeVtx edgeInfo = (EdgeVtx) condDepGraph.getDGNode(edge.hashCode());  // 根据当前边获取边内读写信息
                    if (!edgeInfo.getgReadVars().isEmpty()) {
                        val = delayVar.get(var);
                        delayVar.remove(var);
                        delayVar.put(var, cedge);
                        return val;
                    }
                } else {
                    val = delayVar.get(var);
                    delayVar.remove(var);
                    return val;
                }
            }

        }
        return val;
    }

}
