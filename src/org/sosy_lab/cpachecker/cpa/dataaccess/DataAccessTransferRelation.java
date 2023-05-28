package org.sosy_lab.cpachecker.cpa.dataaccess;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.cfa.model.*;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionReturnEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CReturnStatementEdge;
import org.sosy_lab.cpachecker.core.defaults.SingleEdgeTransferRelation;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.cpa.callstack.CallstackState;
import org.sosy_lab.cpachecker.cpa.threadingintp.ThreadingIntpState;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.dependence.conditional.ConditionalDepGraph;
import org.sosy_lab.cpachecker.util.dependence.conditional.EdgeVtx;
import org.sosy_lab.cpachecker.util.dependence.conditional.Var;
import org.sosy_lab.cpachecker.util.globalinfo.EdgeInfo;
import org.sosy_lab.cpachecker.util.globalinfo.GlobalInfo;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.cpachecker.util.statistics.ThreadSafeTimerContainer;

import java.util.*;

@Options(prefix = "cpa.dataaccess")
public class DataAccessTransferRelation extends SingleEdgeTransferRelation {
    private EdgeInfo edgeInfo;   // 用于获取主函数名
    private ConditionalDepGraph conDepGraph;  // 用于获取边上信息
    private Configuration config;
    private Statistics stats;
    private ThreadSafeTimerContainer.TimerWrapper transferTimer;

    @Option(secure = true, name = "MAIN_LINE", description = "The main function begin line")
    private int MAIN_LINE;

    boolean reachMainFunc;

    public DataAccessTransferRelation(Configuration pConfig, Statistics pStatistics) throws InvalidConfigurationException {
        pConfig.inject(this);
        conDepGraph = GlobalInfo.getInstance().getEdgeInfo().getCondDepGraph();
        edgeInfo = GlobalInfo.getInstance().getEdgeInfo();
        config = pConfig;
        stats = pStatistics;
        transferTimer = ((DataAccessStatistics) stats).transferTimer.getNewTimer();
        reachMainFunc = false;
    }

    public Statistics getStatistics() {
        return stats;
    }

    @Override
    public Collection<DataAccessState> getAbstractSuccessorsForEdge(AbstractState pstate, Precision precision, CFAEdge pCfaEdge) throws CPATransferException, InterruptedException {
        DataAccessState lastDataAccess = (DataAccessState) pstate;
        return Collections.singleton(lastDataAccess);
    }

    @Override
    public Collection<? extends AbstractState> strengthen(AbstractState state, Iterable<AbstractState> otherStates, @Nullable CFAEdge cfaEdge, Precision precision) throws CPATransferException, InterruptedException {
        transferTimer.start();

        try {
            // 将父节点中的 Dataaccess 取出
            DataAccessState lastDataAccess = (DataAccessState) state;


            // 若没进入函数主题部分，则不进行冲突性检测
            if (!reachMainFunc) {
                reachMainFunc = cfaEdge.getFileLocation().equals(edgeInfo.getCfa().getMainFunction().getFileLocation());
                return Collections.singleton(lastDataAccess);
            }
            String mainFunction = edgeInfo.getCfa().getMainFunction().getFunctionName();

//             use for get callstack
            List<String> stack = new ArrayList<>();
            String topFunc = null;
            for (ThreadingIntpState threadingIntpState : AbstractStates.projectToType(otherStates, ThreadingIntpState.class)) {
                Preconditions.checkNotNull(threadingIntpState);
                String activeThread = threadingIntpState.getActiveThread();
                CallstackState callstack = (CallstackState) threadingIntpState.getThreadCallstack(activeThread);
                stack = callstack.getStack();
            }
            for (String funcName : stack) {
                if (funcName.contains("isr")) {
                    topFunc = funcName;
                    break;
                }
            }
            if (topFunc == null) {
                topFunc = stack.get(0);
            }

//            Deque<String> intpStack = null;
//            String topFunc = null;
//            for (ThreadingIntpState threadingIntpState : AbstractStates.projectToType(otherStates, ThreadingIntpState.class)) {
//                Preconditions.checkNotNull(threadingIntpState);
//                 intpStack = threadingIntpState.getIntpStack();
//            }
//            if (intpStack == null  || intpStack.isEmpty()) {
//               topFunc = mainFunction;
//            }else{
//                topFunc = intpStack.pop();
//            }
//
//            if (lastDataAccess.getPathFunc().isEmpty()) {
//                lastDataAccess.setPathFunc(mainFunction);
//            }

            String task1 = null;
            if (cfaEdge instanceof CReturnStatementEdge || cfaEdge instanceof FunctionExitNode|| cfaEdge instanceof CFunctionReturnEdge || cfaEdge.toString().contains("default return")) {
                lastDataAccess.setPathFunc(topFunc, cfaEdge.getPredecessor().getFunctionName());
                task1 = cfaEdge.getSuccessor().getFunctionName();
            }

            EdgeVtx edgeVtx = (EdgeVtx) conDepGraph.getDGNode(cfaEdge.hashCode());
            // 如果边信息为空，则直接返回父节点信息
            if (edgeVtx == null) {
                return Collections.singleton(lastDataAccess);
            }

            DataAccessState dataAccess = new DataAccessState(lastDataAccess.getDataAccess(), lastDataAccess.getDataRace(), lastDataAccess.getPathFunc());

            // 得到读写信息
            Set<Var> gRVars = edgeVtx.getgReadVars(), gWVars = edgeVtx.getgWriteVars();

            // 得到边所在的函数名
            String task = edgeVtx.getBlockStartEdge().getPredecessor().getFunctionName();
            int times = 1;
            if(!dataAccess.getPathFunc().containsKey(task) && !dataAccess.getPathFunc().containsKey(task1)){
                times = getFuncTimes(topFunc, dataAccess.getPathFunc(), task);
            }

            // 先判断读   因为对于任何一条语句， 无论怎样都是先读后写
            if (!gRVars.isEmpty()) {
                for (Var var : gRVars) {

                    //如果变量已经被检测过了，则不在检测
                    if (dataAccess.isInDataRace(var.getName())) continue;
                    int location = var.getExp().getFileLocation().getEndingLineNumber();

                    State ec = null;
                    CFAEdgeType edgeType = cfaEdge.getEdgeType();
                    switch(cfaEdge.getEdgeType()){
                        case FunctionCallEdge:
                            task = stack.get(stack.size()-1);   // 即将进入的函数
                            task1 = stack.get(stack.size()-2);  // 当前调用函数
                            ec = new State(var.getName(), task1,location,"R",topFunc,times);
                            dataAccess.DataRace(ec, mainFunction);
                            ec = new State(var.getName(), task,location,"R",topFunc,times);
                            break;
                        case FunctionReturnEdge:
                            ec = new State(var.getName(), task1, location, "R", topFunc, times);
                            break;
                        default:
                            ec = new State(var.getName(), task, location, "R", topFunc, times);
                    }

                    Preconditions.checkNotNull(ec);
                    dataAccess.DataRace(ec, mainFunction); // 进行数据冲突检测
                }
            }

            // 后判断写
            if (!gWVars.isEmpty()) {
                for (Var var : gWVars) {

                    if (dataAccess.isInDataRace(var.getName())) continue;
                    int location = var.getExp().getFileLocation().getEndingLineNumber();

                    State ec = null;
                    CFAEdgeType edgeType = cfaEdge.getEdgeType();
                    switch(cfaEdge.getEdgeType()){
                        case FunctionReturnEdge:
                            ec = new State(var.getName(), task1, location, "W", topFunc, times);
                            break;
                        default:
                            ec = new State(var.getName(), task, location, "W", topFunc, times);
                    }

                    Preconditions.checkNotNull(ec);
                    dataAccess.DataRace(ec, mainFunction); // 进行数据冲突检测
                }
            }

            return Collections.singleton(dataAccess);
        } finally {
            // anyway, stop the timer when transfer process finished.
            transferTimer.stop();
        }
    }

    private int getFuncTimes(String topFunc, Map<String, List<String>> pathFunc, String task) {
        int cnt = 1;
        if (pathFunc.containsKey(topFunc)) {   // current path already has the corresponding data
            cnt = getTimes(pathFunc.get(topFunc), task);
//            pathFunc.get(topFunc).add(task);
            return cnt;
        } else {
            cnt = 1;
            List<String> funcList = new ArrayList<>();
            funcList.add(task);
//            pathFunc.put(topFunc, funcList);
            return cnt;
        }
    }

    private int getTimes(List<String> funcPath, String task) {
        int cnt = 1;
        for (String func : funcPath)
            if (func == task)
                cnt++;

        return cnt;
    }


}
