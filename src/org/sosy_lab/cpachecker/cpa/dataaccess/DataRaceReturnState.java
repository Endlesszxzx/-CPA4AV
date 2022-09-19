package org.sosy_lab.cpachecker.cpa.dataaccess;

import java.util.ArrayList;
import java.util.List;

public class DataRaceReturnState {
    private DataState actionList;
    private DataState race;

    public DataRaceReturnState() {
    }
    public DataRaceReturnState(DataState actionList) {
        this.actionList = actionList;
        race = null;
    }

    public DataRaceReturnState(DataState actionList, DataState race) {
        this.actionList = actionList;
        this.race = race;
    }

    public DataState getActionList() {
        return actionList;
    }

    public DataState getRace() {
        return race;
    }

    @Override
    public String toString() {
        return "DataRaceReturnState{" +
                "Actionlist=" + actionList.toString() +
                ", race=" + race.toString() +
                '}';
    }
}
