package org.sosy_lab.cpachecker.cpa.dataaccess;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Graphable;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

//public class DataAccessState implements AbstractState, Graphable {
public class DataAccessState implements AbstractState {
    private List<DataState> dataAccess;

    private List<DataState> dataRace;

    private boolean isRace = false;

    private Map<String, List<String>> pathFunc;

    public Map<String, List<String>> getPathFunc() {
        return pathFunc;
    }

    public void setPathFunc(Map<String, List<String>> pathFunc) {
        this.pathFunc = pathFunc;
    }

    public void setPathFunc(String mainFunc) {
        List<String> func = new ArrayList<>();
        func.add(mainFunc);
        pathFunc.put(mainFunc, func);
    }

    public void setPathFunc(String topFunc, String funcName) {
        if (pathFunc.containsKey(topFunc)) {
            pathFunc.get(topFunc).add(funcName);
        } else {
            List<String> func = new ArrayList<>();
            func.add(funcName);
            pathFunc.put(topFunc, func);
        }
    }

    public boolean isRace() {
        return isRace;
    }

    public void setRace(boolean race) {
        isRace = race;
    }


// 构造方法

    public static DataAccessState getInitialInstance() {
        return new DataAccessState();
    }

    public DataAccessState() {
        dataAccess = new ArrayList<DataState>();
        dataRace = new ArrayList<DataState>();
        pathFunc = new HashMap<String, List<String>>();
    }

    //
    public DataAccessState(List<DataState> dataAccess, List<DataState> dataRace, Map<String, List<String>> pathFunc) {
        this.dataAccess = newData(dataAccess);
        this.dataRace = newData(dataRace);
        this.pathFunc = newPath(pathFunc);
    }

    private Map<String, List<String>> newPath(Map<String, List<String>> pathFunc) {
        Map<String, List<String>> ans = new HashMap<String, List<String>>();
        for (String task : pathFunc.keySet()) {
            List<String> tmp = new ArrayList<>(pathFunc.get(task));
            ans.put(task, tmp);
        }
        return ans;
    }

    public List<DataState> newData(List<DataState> dataAccess) {
        List<DataState> res = new ArrayList<DataState>();
        for (DataState tmp : dataAccess) {
            res.add(new DataState(tmp.getN(), tmp.getA()));
        }
        return res;
    }


    // dataAccess的方法

    public List<DataState> getDataAccess() {
        return dataAccess;
    }

    public void setDataAccess(DataState dataAccess) {
        this.dataAccess.get(actionListPosition(dataAccess.getN())).setAll(dataAccess);
    }

    public void setDataAccess(State ec) {
        int index = this.actionListPosition(ec.getName());

        if (index == -1) {
            DataState e = new DataState(ec.getName(), ec);
            dataAccess.add(e);
            return;
        }

        dataAccess.get(index).append(ec);
    }

    public void add(DataState e) {
        dataAccess.add(e);
    }


    public int actionListPosition(String Name) {
        /**
         * 判断当前 Name 是否在数据访问集中, 在返回序号，不在返回-1
         */

        for (int i = 0; i < dataAccess.size(); i++) {
            if (dataAccess.get(i).getN().equals(Name)) return i;
        }

        return -1;
    }

    // dataRace 的方法
    public List<DataState> getDataRace() {
        return dataRace;
    }

    public boolean isInDataRace(String Name) {
        for (DataState data : dataRace) {
            if (data.getN() == Name) {
                return true;
            }
        }
        return false;
    }

    public void DataRace(State ec, String mainFunction) {
        /**
         * 数据冲突检测
         * @param ActionList 与 ec 同名的共享变量之前的所有操作集合
         * @param ec 当前状态 ec
         * @return 返回一个数据冲突对 (ep,er,ec)，或返回一个 null
         */

        int index = this.actionListPosition(ec.getName());

        if (index == -1) {
            DataState e = new DataState(ec.getName(), ec);
            dataAccess.add(e);
            return;
        }

        DataState actionList = dataAccess.get(index);

        String[][] patternList = {{"R", "W", "R"}, {"W", "W", "R"}, {"R", "W", "W"}, {"W", "R", "W"}};    // 冲突模式集

        // 判断 变量ec.Name 的 DataAccess 的大小，小于2，直接加入
        if (actionList.getA().size() < 2) {
            actionList.append(ec);
            return;
        }

        Interruptinformation get_race = actionList.get_ep(ec);   // 倒序搜索 ep
        if (get_race.getepPosition() == -1) {     // 没找到相应的 ep
            actionList.updateDataAccess(get_race.getepPosition(), ec, 2, mainFunction);
            return;
        }

        // 找到了相应的 ep， 找出合适的 er， 查看是否会产生冲突
        State ep = actionList.getA().get(get_race.getepPosition());
        for (int i = 0; i < get_race.getInterOperation().size(); i++) {
            State er = get_race.getInterState().get(i);

            String[] pattern = {ep.getAction(), er.getAction(), ec.getAction()};

            // 冲突检测，是否有 patternList 中的 pattern
            for (int j = 0; j < 4; j++) {

//                if (Arrays.equals(patternList[j], pattern) && ep.getLoaction() != ec.getLoaction()) {
                if (Arrays.equals(patternList[j], pattern)) {
                    actionList.updateDataAccess(get_race.getepPosition(), ec, 0, mainFunction);
                    isRace = true;
                    DataState race = new DataState(ep.getName(), ep);
                    race.append(er);
                    race.append(ec);
                    dataRace.add(race);

                    String str = race.toString();
                    if (j == 0) {
                        DataAccessCPA.raceNum.setraceRWRSet(str);
                    } else if (j == 1) {
                        DataAccessCPA.raceNum.setraceWWRSet(str);
                    } else if (j == 2) {
                        DataAccessCPA.raceNum.setraceRWWSet(str);
                    } else if (j == 3) {
                        DataAccessCPA.raceNum.setraceWRWSet(str);
                    }
                    DataAccessCPA.raceNum.setRace(str);
                }
            }
        }

        if (isRace) {
            return;
        }

        // 当都不发生数据冲突时
        actionList.updateDataAccess(get_race.getepPosition(), ec, 1, mainFunction);
    }

    @Override
    public String toString() {
        return "DataAccessState{" +
                "dataAccess=" + dataAccess +
                ",\n dataRace=" + dataRace +
                ",\n isRace=" + isRace +
                ",\n pathFunc=" + pathFunc +
                '}';
    }

//    @Override
//    public String toDOTLabel() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("\n--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
//        sb.append(this.toString());
//        return sb.toString();
//    }
//
//    @Override
//    public boolean shouldBeHighlighted() {
//        return false;
//    }


}


