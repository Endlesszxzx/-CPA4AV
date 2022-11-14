package org.sosy_lab.cpachecker.cpa.threadingintp;

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;

import java.util.HashSet;
import java.util.Set;

public class DelayStrategy {
    private String var;
    private CFAEdge cfaEdge;
    private Set<String> intpFunc;

    public DelayStrategy(String var, CFAEdge cfaEdge, Set<String> intpFunc) {
        this.var = var;
        this.cfaEdge = cfaEdge;
        this.intpFunc = new HashSet<>(intpFunc);
    }

    public DelayStrategy reNew(){
        return new DelayStrategy(var,cfaEdge,intpFunc);
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public CFAEdge getCfaEdge() {
        return cfaEdge;
    }

    public void setCfaEdge(CFAEdge cfaEdge) {
        this.cfaEdge = cfaEdge;
    }

    public Set<String> getIntpFunc() {
        return intpFunc;
    }

    public void setIntpFunc(Set<String> intpFunc) {
        this.intpFunc = intpFunc;
    }

    public void drop(Set<String> intp){
        intpFunc.removeAll(intp);
    }

    public void removeIntpFunc(String intpFunc){
        this.intpFunc.remove(intpFunc);
    }

    @Override
    public String toString() {
        return  "var='" + var + '\'' +
                ", cfaEdge=" + cfaEdge +
                ", intpFunc=" + intpFunc ;
    }
}
