package org.sosy_lab.cpachecker.cpa.threadingintp;

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;

import java.util.HashSet;
import java.util.Set;

public class DelayStrategy {
    private String var;
    private CFAEdge cfaEdge;
    private Set<String> intpFunc;

    private Set<String> disableFunc;

    public DelayStrategy(String var, CFAEdge cfaEdge, Set<String> intpFunc,Set<String> disableFunc) {
        this.var = var;
        this.cfaEdge = cfaEdge;
        this.intpFunc = new HashSet<>(intpFunc);
        this.disableFunc = new HashSet<>(disableFunc);
    }

    public DelayStrategy reNew(){
        return new DelayStrategy(var,cfaEdge,intpFunc,disableFunc);
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

    public Set<String> getDisableFunc() {
        return disableFunc;
    }

    public void setDisableFunc(Set<String> disableFunc) {
        this.disableFunc = disableFunc;
    }
    public void setDisableFunc(String disableFunc) {
        this.disableFunc.add(disableFunc);
    }

    public void removeDisableFunc(String intpFunc){
        if(disableFunc == null){
            disableFunc = new HashSet<>();
        }
        this.disableFunc.remove(intpFunc);
    }

    @Override
    public String toString() {
        return "var='" + var + '\'' +
                ", cfaEdge=" + cfaEdge +
                ", intpFunc=" + intpFunc +
                ", disableFunc=" + disableFunc;
    }
}
