package org.sosy_lab.cpachecker.cpa.dataaccess;

public class State {
    /**
     * 每一个节点应包含的状态
     */
    private String Name;
    private String Task;
    private int Loaction;
    private String topfunc;
    private String Action;

    private int Times;

    public int getTimes() {
        return Times;
    }

    public void setTimes(int times) {
        Times = times;
    }

    public String getTopfunc() {
        return topfunc;
    }

    public void setTopfunc(String ISR) {
        this.topfunc = ISR;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTask() {
        return Task;
    }

    public void setTask(String task) {
        Task = task;
    }

    public int getLoaction() {
        return Loaction;
    }

    public void setLoaction(int loaction) {
        Loaction = loaction;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }


    public State() {
    }

    public State(String name, String task, int loaction, String action, String isr,int times) {
        Name = name;
        Task = task;
        Loaction = loaction;
        Action = action;
        topfunc = isr;
        Times = times;
    }

    @Override
    public String toString() {
        return "(" + Name + "," + Task + ", '" + Loaction + ", '" + Action + ", " + topfunc + ", " + Times +')';
    }


}
