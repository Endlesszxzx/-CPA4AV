package org.sosy_lab.cpachecker.cpa.dataaccess;

import java.util.ArrayList;
import java.util.List;

public class DataState {
    /**
     * 数据访问集中存放的类型及相关方法
     */
    private String N;
    private List<State> A;

    public DataState(){

    }

    public DataState(String Name) {
        N = Name;
        A = new ArrayList<State>();
    }

    public DataState(String Name, State e) {
        N = Name;
        A = new ArrayList<State>();
        A.add(e);
    }

    public DataState(String N, List<State> A) {
        this.N = N;
        this.A = new ArrayList<>(A);
    }


    public String getN() {
        return N;
    }

    public void setN(String n) {
        N = n;
    }

    public List<State> getA() {
        return A;
    }

    public void setA(List<State> a) {
        A = a;
    }



    public boolean isEmpty() {
        return N.isEmpty() && A.isEmpty();
    }

    public void append(State e) {
        A.add(e);
    }

    public int length() {
        return A.size();
    }


    public void setAll(DataState e) {
        N = e.N;
        A = e.A;
    }

    public void getEmpty() {
        A = new ArrayList<State>();
    }

    public void updateDataAccess(Integer ep_position, State ec, Integer flag, String mainFunction) {
        // 未跳出中断，仍在执行中断，或在主函数中
        if (flag == 2 || flag == 1 ) {
            if (ec.getTask() == mainFunction) {
                // 删除： 中断内的所有操作 + 状态 ep
                A = new ArrayList<State>();
                this.append(ec);
            } else {
                this.append(ec);
            }
        }

        // 产生了数据冲突
        else if (flag == 0) {
            this.getEmpty();
            this.append(ec);
        }

    }

    public Interruptinformation get_ep(State ec) {
        Interruptinformation ans = new Interruptinformation();

        // 倒序搜索 ep
        for (int i = A.size() - 1; i >= 0; i--) {
            State ep = A.get(i);

            if(ep.getTask() == ec.getTask() && ep.getTopfunc() == ec.getTopfunc() && ep.getTimes()==ec.getTimes()){
                ans.setepPosition(i);
                return ans;
            }
            // 没找到，但找到了中断中对共享变量的其它操作
            if(ep.getTopfunc().contains("isr")){
                ans.setInterLocation(ep.getTask());
                ans.setInterOperation(ep.getAction());
                ans.setInter_state(ep);
            }
        }

        return ans;
    }


    @Override
    public String toString() {
        return "\n" +
                N + ':' + toprint() +
                '}';
    }

    public String toprint(){
        StringBuffer str = new StringBuffer();
        for(int i=0;i<A.size();i++){
            if(i==0){
                str.append(A.get(i));
                str.append("\n");
                continue;
            }
            str.append("                               "+A.get(i));
            str.append("\n");
        }
        return String.valueOf(str);
    }

    public boolean isSame(DataState data){
        String str1 = data.toString();
        String str2 = this.toString();

        if(str1.equals(str2)){
            return true;
        }
        return false;
    }
}
