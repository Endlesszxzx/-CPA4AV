// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.cpachecker.cpa.threadingintp;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.Optionals;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.*;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCall;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CIntegerLiteralExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CUnaryExpression;
import org.sosy_lab.cpachecker.cfa.model.*;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionCallEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.c.CReturnStatementEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.cfa.postprocessing.global.CFACloner;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.core.defaults.SingleEdgeTransferRelation;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.StateSpacePartition;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonState;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonVariable;
import org.sosy_lab.cpachecker.cpa.callstack.CallstackCPA;
import org.sosy_lab.cpachecker.cpa.location.LocationCPA;
import org.sosy_lab.cpachecker.cpa.threading.GlobalAccessChecker;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCodeException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.automaton.AutomatonGraphmlCommon.KeyDef;
import org.sosy_lab.cpachecker.util.dependence.conditional.ConditionalDepGraph;
import org.sosy_lab.cpachecker.util.dependence.conditional.EdgeVtx;
import org.sosy_lab.cpachecker.util.dependence.conditional.Var;
import org.sosy_lab.cpachecker.util.globalinfo.EdgeInfo;
import org.sosy_lab.cpachecker.util.globalinfo.GlobalInfo;

@Options(prefix = "cpa.threadingintp")
public final class ThreadingIntpTransferRelation extends SingleEdgeTransferRelation {

    @Option(description = "do not use the original functions from the CFA, but cloned ones. " + "See cfa" + ".postprocessing.CFACloner for detail.", secure = true)
    private boolean useClonedFunctions = true;

    @Option(description = "allow assignments of a new thread to the same left-hand-side as an existing thread.", secure = true)
    private boolean allowMultipleLHS = false;

    @Option(description = "the maximal number of parallel threads, -1 for infinite. " + "When combined with " + "'useClonedFunctions=true', we " + "need" + " at least N cloned functions. " + "The option 'cfa.cfaCloner" + ".numberOfCopies' should be set to N.", secure = true)
    private int maxNumberOfThreads = 5;

    @Option(description = "atomic locks are used to simulate atomic statements, as described in the rules of SV-Comp" + ".", secure = true)
    private boolean useAtomicLocks = true;

    @Option(description = "local access locks are used to avoid expensive interleaving, if a thread only reads and writes its own variables.", secure = true)
    private boolean useLocalAccessLocks = true;

    @Option(description = "in case of witness validation we need to check all possible function calls of cloned CFAs" + ".", secure = true)
    private boolean useAllPossibleClones = false;

    private boolean isVerifyingConcurrentProgram = false;

    @Option(secure = true, description = "use increased number for each newly created same thread." + "When this " + "option is enabled, we need " + "not to clone a thread " + "function many times if " + "every thread function is " + "only used once (i.e., cfa.cfaCloner.numberOfCopies can" + " be set " + "to 1).")
    private boolean useIncClonedFunc = false;

    @Option(secure = true, description = "simulate the interruption.")
    private boolean simulateInterruption = true;

    @Option(secure = true, description = "The function name of disabling interrupt.")
    private String disIntpFunc = "disable_isr";

    @Option(secure = true, description = "The function name of enabling interrupt.")
    private String enIntpFunc = "enable_isr";

    @Option(secure = true, description = "This folder contains the priority files that have the same prefix with the " + "main function.")
    private String priorityFileFolder = "./config/";
    @Option(secure = true, description = "This string spefies the file extension of the priority file name.")
    private String priorityFileExtSuffix = "_priority.txt";
    @Option(secure = true, description = "The regex for extracting the interruption priority from a file.")
    private String priorityRegex = "^([a-zA-Z_]+[a-zA-Z0-9_]*)\\s+(\\d+)";  //正则表达
    @Option(secure = true, description = "Which order of interruption priority to use?")
    private InterruptPriorityOrder intpPriOrder = InterruptPriorityOrder.BH;

    @Option(secure = true, description = "Whether enable interrupt nesting (i.e., allow high-priority interrupts " + "during processing a " + "low" + "-priority interrupt). " + "NOTICE: this option may cause expensive interleavings!")
    private boolean enableInterruptNesting = false;
    @Option(secure = true, description = "This option limits the maximun level of interrupt nesting.")
    private int maxLevelInterruptNesting = 2;

    @Option(secure = true, description = "Whether regard current input program as sequence program, or not. When " + "verifying interrupt" + "program in sequence program, this value need to be true to avoid error from " + "method" + "'getActiveThread'")
    private boolean isSingleThread = true;

    @Option(secure = true, description = "Whether allow a interrupt A to be interrupted by the same interrupt (i.e., the reentrability " + "of" + " a interrupt). NOTICE: currently, this feature is not supported!")
    private boolean allowInterruptReentrant = false;

    @Option(secure = true, description = "Wheter enable the optimization streategy of representative point selecting. (i.e., if not enable this streategy, then all the interruptions can be triggered at almost all the program locations)")
    private boolean enableRepPointSelecting = true;

    /**
     * The interruption priorities of all the functions obtained from the file 'InterruptPriority.txt'
     * will put into this map. The rules for interruption are:
     * <p>
     * 1. When the CPU receives several interrupts at the same time, it first responds to the
     * interruption request with the highest priority; 2. Ongoing interruption processes cannot be
     * interrupted by new interrupt requests of the same level or lower priority; 3. Ongoing
     * low-priority interrupt services can be interrupted by high-priority interrupt requests.
     * </p>
     * <p>
     * {\<function_name, priority_number\>, ...}
     * </p>
     */
    private static Map<String, Integer> priorityMap;

    @Option(secure = true, description = "This option limits the maximum times for each interruption " + "function " + "(-1 for un-limit times of " + "interruption).")
    private int maxInterruptTimesForEachFunc = 1;

    /**
     * We only need to add interruption at some special points (namely represent points).
     * <p>
     * {\< location, {interrupt_function_name, ...}\>, ...}
     * </p>
     */
    private static Map<CFANode, Set<String>> repPoints;

    public enum InterruptPriorityOrder {
        SH, // the smaller a priority number of an interruption function is, the higher of its priority
        // is;
        BH // the bigger a priority number of an interruption function is, the higher of its priority
        // is.
    }

    public static final String THREAD_START = "pthread_create";
    public static final String THREAD_JOIN = "pthread_join";
    private static final String THREAD_EXIT = "pthread_exit";
    private static final String THREAD_MUTEX_LOCK = "pthread_mutex_lock";
    private static final String THREAD_MUTEX_UNLOCK = "pthread_mutex_unlock";
    private static final String VERIFIER_ATOMIC = "__VERIFIER_atomic_";
    private static final String VERIFIER_ATOMIC_BEGIN = "__VERIFIER_atomic_begin";
    private static final String VERIFIER_ATOMIC_END = "__VERIFIER_atomic_end";
    private static final String ATOMIC_LOCK = "__CPAchecker_atomic_lock__";
    private static final String LOCAL_ACCESS_LOCK = "__CPAchecker_local_access_lock__";
    private static final String THREAD_ID_SEPARATOR = "__CPAchecker__";
    public static final String INTERRUPT_LOCK = "__CPAchecker_interrupt_lock__";
    public static final String INTERRUPT_ID_PREFIX = "__CPAchecker_interrupt_id_";

    private static final ImmutableSet<String> THREAD_FUNCTIONS = ImmutableSet.of(THREAD_START, THREAD_MUTEX_LOCK, THREAD_MUTEX_UNLOCK, THREAD_JOIN, THREAD_EXIT, VERIFIER_ATOMIC_BEGIN, VERIFIER_ATOMIC_END);

    private final CFA cfa;
    private final LogManager logger;
    private final CallstackCPA callstackCPA;
    private final ConfigurableProgramAnalysis locationCPA;

    private final GlobalAccessChecker globalAccessChecker = new GlobalAccessChecker();

    private final ConditionalDepGraph condDepGraph;
    private Map<String, Map<String, Set<String>>> intpFuncRWSharedVarMap;
    private boolean reachMainFunc;
    private EdgeInfo edgeInfo;

    public ThreadingIntpTransferRelation(Configuration pConfig, CFA pCfa, LogManager pLogger) throws InvalidConfigurationException {
        pConfig.inject(this);
        cfa = pCfa;
        locationCPA = LocationCPA.create(pCfa, pConfig);
        callstackCPA = new CallstackCPA(pConfig, pLogger);
        logger = pLogger;
        condDepGraph = GlobalInfo.getInstance().getEdgeInfo().getCondDepGraph();   // 获取约束依赖图，用于判断两边之间是否有相同共享变量时使用
        reachMainFunc = false;
        edgeInfo = GlobalInfo.getInstance().getEdgeInfo();

        priorityMap = parseInterruptPriorityFile();  // 读取中断优先级文件  存储形式为 ： Map<中断函数名, 优先级>
        if (enableRepPointSelecting) {   // 使用中断点
            repPoints = buildRepPointMap();      // buildRepPointMap() 获取可进行中断点处的可插入函数返回类型为：Map<中断点, Set<可插入中断函数名>>
        } else {    // 不使用中断点
            repPoints = buildFullRepPointMap();  // buildFullRepPointMap() 遍历所有的函数(包括中断函数)，将所有的节点和 priorityMap 中的中断函数绑定到一起
        }
        // repPoints = Map<中断点, Set<中断函数名>>  表示为在中断点可触发的中断函数 Set<中断函数名>

        // 需要注意的是，无论是否选择中断点，都是用变量 enterFuncBody 来判断是否进入函数体内。只有函数体内的点，才会选择作为中断点。

        /* added by yzc 2022.07.03: 使用分隔符__cloned_function__来判断是否是验证并行程序 */
        isVerifyingConcurrentProgram = !from(cfa.getAllFunctionNames()).filter(f -> f.contains(CFACloner.SEPARATOR)).toSet().isEmpty();
    }


    // TODO: this function currently is only for testing interruption implementation.
    // 获取可进行中断点处的可插入函数返回类型为：Map<中断点, Set<中断函数名>>
    private Map<CFANode, Set<String>> buildRepPointMap() {
        //// first step: get all the global access variables of interruption functions.  获取所有中断函数的全局访问变量
        intpFuncRWSharedVarMap = getIntpFuncReadWriteSharedVarMap();  // Map<中断函数名，Set<共享变量>>

        //// second step: iterate all the edges of 'main' function & the interruption functions. 迭代 'main' 函数和中断函数的所有边。
        Map<CFANode, Set<String>> results = new HashMap<>();   // Map<中断选择点，Set<可插入的中断函数名>>

        //// third step 1: process main function - interruption function
        Set<String> procFuncs = new HashSet<>();   // Set<待处理的函数名>
        procFuncs.add(cfa.getMainFunction().getFunctionName());   // 首先处理 main 函数
        results = handleRepPointForFunctions(procFuncs, results);  // handleRepPointForFunctions 用于处理

        //// third step 2: process interruption function - interruption function  处理所有的中断函数
        procFuncs.clear();
        procFuncs.addAll(priorityMap.keySet());
        results = handleRepPointForFunctions(procFuncs, results);

        //// forth step: process the case that the main function and the functions it called do not
        //// access any shared variable.
        //// 处理主函数及其调用的函数均与中断函数没有公共访问的全局变量的情况   即主函数程序体内没有共享变量的情况
        results = handleRepPointCaseEmptyResults(results);

        return results;
    }

    private Map<CFANode, Set<String>> buildFullRepPointMap() {
        // 遍历所有的函数(包括中断函数)，将所有的节点和 priorityMap 中的中断函数绑定到一起
        Map<CFANode, Set<String>> results = new HashMap<>();

        for (String func : cfa.getAllFunctionNames()) {
            Set<CFANode> visitedNodes = new HashSet<>();
            Deque<CFANode> waitlist = new ArrayDeque<>();

            // obtain the enter point of current function.
            FunctionEntryNode funcEntry = cfa.getFunctionHead(func);
            if (funcEntry == null) {
                // we could not find the definition of this function, it means that this function is only
                // has been declared.
                continue;
            }

            waitlist.push(funcEntry);
            // except for the main function, other functions will immediately enter the function body.
            boolean enterFuncBody = false;
            while (!waitlist.isEmpty()) {
                CFANode node = waitlist.pop();

                if (!visitedNodes.contains(node)) {
                    // process each successor edge of current node.
                    for (int i = 0; i < node.getNumLeavingEdges(); ++i) {
                        CFAEdge edge = node.getLeavingEdge(i);

                        // the function enters the function-body only if an edge with the description 'Function start dummy edge' is reached.
                        if (edge.getDescription().equals("Function start dummy edge")) {
                            enterFuncBody = true;
                        }

                        // we add all the interruptions into the set of current program location to simulate the
                        // triggering.
                        // NOTICE: we should not add interruptions into the set of entry (with description
                        // 'Function start dummy edge' for its first successor edge) and exit nodes.
//            if (enterFuncBody
//                && !(node instanceof FunctionEntryNode)
//                && !(node instanceof FunctionExitNode)
//                && !edge.getDescription().equals("Function start dummy edge")) {
//              results.put(node, priorityMap.keySet());
//            }
                        if (enterFuncBody) {
                            results.put(node, priorityMap.keySet());
                        }

                        visitedNodes.add(node);
                        waitlist.push(edge.getSuccessor());
                    }
                }
            }

        }

        return results;
    }

    private Map<CFANode, Set<String>> handleRepPointForFunctions(Set<String> pProcFuncSet,      // Set<待处理函数名>
                                                                 Map<CFANode, Set<String>> pResults  // Map<中断插入点，Set<可插入函数>>
    ) {

        // iterate all the interruption functions.  迭代所有中断函数。
        Iterator<String> intpFuncIter = pProcFuncSet.iterator(); // 当前 intpFuncIter 内仅有主函数
        while (intpFuncIter.hasNext()) {
            String curIntpFunc = intpFuncIter.next();   // 获取下一个函数

            Deque<String> waitFuncList = new ArrayDeque<>();
            waitFuncList.push(curIntpFunc);

            Set<CFANode> visitedNodes = new HashSet<>();
            Deque<CFANode> waitlist = new ArrayDeque<>();

            while (!waitFuncList.isEmpty()) {
                // 对于每个取出来的函数 f ，遍历其所有的 CFANode 对每一个节点，获取该节点的所有出边并逐一处理。 若出边 e 中包含的全局变量和 intpFuncRWSharedVarMap
                // 中的全局变量有交集，则将边 e 的前驱节点标记为中断点， 并将相应的中断函数和该中断点绑定。
                String curFunc = waitFuncList.pop();

                // obtain the entry point of current function. 获得当前函数的入口点
                FunctionEntryNode funcEntry = cfa.getFunctionHead(curFunc);
                if (funcEntry == null) {
                    // we could not find the definition of this function, it means that this function is only has
                    // been declared.
                    // 我们找不到这个函数的定义，这意味着这个函数只是被声明了
                    continue;
                }

                waitlist.push(funcEntry);
                boolean isEnterFuncBody = false;
                while (!waitlist.isEmpty()) {
                    CFANode node = waitlist.pop();  // 拿到函数入口点

                    if (!visitedNodes.contains(node)) {
                        for (int i = 0; i < node.getNumLeavingEdges(); ++i) {  // 遍历出边
                            CFAEdge edge = node.getLeavingEdge(i);

                            // the first blank-edge is.  进入函数的第一条边
                            if (!isEnterFuncBody && (edge.getDescription().equals("Function start dummy edge"))) {
                                isEnterFuncBody = true;
                            }

                            // enter the body of the current function.  进入当前函数的函数体
                            if (isEnterFuncBody) {
                                EdgeVtx edgeInfo = (EdgeVtx) condDepGraph.getDGNode(edge.hashCode());  // 根据当前边获取边内读写信息
                                // obtain additional information: the called function in this edge; the pre/sucNode of this edge.
                                String callFuncName = getFunctionCallName(edge);
                                CFANode preNode = edge.getPredecessor(), sucNode = edge.getSuccessor();  // 获得 a

                                if (edgeInfo != null) {  // 如果边上拥有实际信息    a edge b
                                    // get read/write variables of the main function edge.   获取主函数边的读写信息
                                    Set<String> edgeRWSharedVarSet = new HashSet<>();
                                    edgeRWSharedVarSet.addAll(from(edgeInfo.getgReadVars()).transform(v -> v.getName()).toSet());
                                    edgeRWSharedVarSet.addAll(from(edgeInfo.getgWriteVars()).transform(v -> v.getName()).toSet());

                                    // NOTICE: we need to add selection point to the successor node of current edge.  我们需要对当前边的后继节点添加选择点
                                    for (String intpFunc : intpFuncRWSharedVarMap.keySet()) {
                                        // if not allow the feature of interrupt reentrant, then we should skip this case.
                                        if (!allowInterruptReentrant && curFunc.equals(intpFunc)) {  // 不允许当前中断函数被当前中断函数所打断 即中断重入
                                            continue;
                                        }

                                        Map<String, Set<String>> intpRWSharedVarSet = intpFuncRWSharedVarMap.get(intpFunc);   // 得到中断函数 intpFunc 内的共享变量

                                        // current edge has accessed some common shared variables that accessed by the
                                        // interruption function. we regard the successor node of current edge as an
                                        // 'representative selection point'.
                                        if (!Sets.intersection(intpRWSharedVarSet.keySet(), edgeRWSharedVarSet).isEmpty()) {  // 两 Set 交集不为空，将 preNode 同 intpFunc 放入 pResults
                                            if (!pResults.containsKey(preNode)) {
                                                pResults.put(preNode, new HashSet<>());
                                            }
                                            pResults.get(preNode).add(intpFunc);
                                        }
                                    }

                                    //// process the function call statement edges.
                                    // if the current function 'a' call another function 'b', we also should analyze
                                    // the global variable access information of the function 'b'.
                                    if (callFuncName != null && !(callFuncName.startsWith(enIntpFunc) || callFuncName.startsWith(disIntpFunc))) {
                                        // 如果函数名不为 enable or disable  那么将当前函数名放入待插函数中
                                        waitFuncList.push(callFuncName);
                                    }
                                }
                                if (callFuncName != null) {
                                    // special process for interruption enable function:
                                    // 	 if the enable_isr function is reached, we may need to add an interruption point.
                                    // NOTICE: we need to add the successor node of the enable_isr function, since the precursor
                                    //         may still disabling the interruptions to be enabled.
                                    if (callFuncName.startsWith(enIntpFunc)) {
                                        pResults = handleDisOrEn(pResults, sucNode, edge);
                                    } else if (callFuncName.startsWith(disIntpFunc)) {
                                        pResults = handleDisOrEn(pResults, preNode, edge);
                                    }
                                }
                            }

                            waitlist.push(edge.getSuccessor());   // 放入边的后继节点
                        }

                        visitedNodes.add(node);          // 将当前节点放入已遍历集合中
                    }
                }
            }
        }

        return pResults;
    }

    private Map<CFANode, Set<String>> handleDisOrEn(Map<CFANode, Set<String>> pResults, CFANode node, CFAEdge edge) {
        if (!pResults.containsKey(node)) {
            pResults.put(node, new HashSet<>());
        }
        // obtain the enabled priority.
        Integer intpPri = getIntpEnablePriority(edge);
        if (intpPri != null) {
            // set the preNode as interruption point when reach the 'enable_isr' function.
            if (intpPri != -1) {
                pResults.get(node).addAll(from(priorityMap.keySet()).filter(f -> priorityMap.get(f) == intpPri).toList());
            } else {
                pResults.get(node).addAll(priorityMap.keySet());
            }
        }
        return pResults;
    }

    private String getFunctionCallName(CFAEdge pEdge) {
        String callFuncName = null;
        if (pEdge instanceof CFunctionCallEdge) {     // 如果边为函数调用，得到被调用的函数名
            CFunctionCallEdge funcCallEdge = (CFunctionCallEdge) pEdge;
            callFuncName = funcCallEdge.getSuccessor().getFunctionName();
        } else if (pEdge instanceof CStatementEdge) {   // 如果边为状态边，得到边上的语句
            CStatementEdge stmtEdge = (CStatementEdge) pEdge;
            CStatement stmt = stmtEdge.getStatement();

            if (stmt instanceof CFunctionCallStatement) {  // 如果语句是函数调用语句，获得被调函数名
                CFunctionCallStatement funcCallStmt = (CFunctionCallStatement) stmt;
                callFuncName = funcCallStmt.getFunctionCallExpression().getFunctionNameExpression().toString();
            } else if (stmt instanceof CFunctionCallAssignmentStatement) {  // 如果语句函数调用赋值语句，获得被调函数名
                CFunctionCallAssignmentStatement funcCallAsgnStmt = (CFunctionCallAssignmentStatement) stmt;
                callFuncName = funcCallAsgnStmt.getFunctionCallExpression().getFunctionNameExpression().toString();
            }
        }
        if (pEdge instanceof BlankEdge) {
//            BlankEdge funcblankEdge
//            callFuncName = "BlankEdge"
        }
        return callFuncName;
    }

    private Map<CFANode, Set<String>> handleRepPointCaseEmptyResults(Map<CFANode, Set<String>> pResults) {
        //// Special process for the program that the main function and the functions it called do not
        //// access the global variables.
        // For this case, we will add all the interruption function to the 'pre-precursor' point of
        // the exit location of the main function.

        // first step: obtain the nodes belongs to the 'main' function.
        String mainFuncName = cfa.getMainFunction().getFunctionName();
        ImmutableSet<CFANode> mainFuncNodes = from(pResults.keySet()).filter(n -> n.getFunctionName().equals(mainFuncName)).toSet();

        if (mainFuncNodes.isEmpty()) {
            // NOTICE: we can not add interruption point at the precursor of the exit node, since this
            // point is meaningless.
            FunctionExitNode mainExitNode = cfa.getMainFunction().getExitNode();
            int numEdgeToExitNode = mainExitNode.getNumEnteringEdges();
            for (int i = 0; i < numEdgeToExitNode; ++i) {
                CFANode preExitNode = mainExitNode.getEnteringEdge(i).getPredecessor();

                int numEdgeToPrePreExitNode = preExitNode.getNumEnteringEdges();
                for (int j = 0; j < numEdgeToPrePreExitNode; ++j) {
                    CFANode prePreExitNode = preExitNode.getEnteringEdge(j).getPredecessor();

                    pResults.put(prePreExitNode, priorityMap.keySet());
                }
            }
        }

        return pResults;
    }

    /**
     * This function collects the shared variables accessed by each interruption function. NOTICE: if
     * interruption function 'a' enables another interruption function 'b', we also add the shared
     * variables that accessed by 'b' into the variable set of 'a'.
     *
     * @return The set of shared variables accessed by each interruption function.
     */
    private Map<String, Map<String, Set<String>>> getIntpFuncReadWriteSharedVarMap() {
        Map<String, Map<String, Set<String>>> results = new HashMap<>();   // use to return

        Set<CFANode> visitedNodes = new HashSet<>();
        Deque<CFANode> waitlist = new ArrayDeque<>();

        // record the interruption functions enable which other interruptions (priority).
        // Map<当前函数名，Set<可被中断函数的优先级>>
        Map<String, Set<Integer>> intpEnableRelation = new HashMap<>();
        for (String func : cfa.getAllFunctionNames()) {
            // we only process the shared variables of interruption functions.
            if (priorityMap.containsKey(removeCloneInfoOfFuncName(func))) {
                Map<String, Set<String>> funcReadWriteSharedVars = new HashMap<>();

                waitlist.push(cfa.getFunctionHead(func));
                while (!waitlist.isEmpty()) {
                    CFANode node = waitlist.pop();

                    if (!visitedNodes.contains(node)) {
                        // iterate all the successor edges of node.
                        for (int i = 0; i < node.getNumLeavingEdges(); ++i) {
                            CFAEdge edge = node.getLeavingEdge(i);
                            if (!visitedNodes.contains(edge.getSuccessor())) {
                                // addIntpEnableRelationForEdge just deal with interruptions function and only meet "enable" will get the real return.
                                // the return is Map<当前函数名，Set<可被中断函数的优先级>>
                                intpEnableRelation = addIntpEnableRelationForEdge(func, edge, intpEnableRelation);

                                // get the access information of global variables of the edge.
                                EdgeVtx edgeInfo = (EdgeVtx) condDepGraph.getDGNode(edge.hashCode());


                                if (edgeInfo != null) { // the edge has global variables
                                    // add by xhr:
                                    Set<Var> gRVars = edgeInfo.getgReadVars(), gWVars = edgeInfo.getgWriteVars();
                                    for (Var var : gRVars) {
                                        if (funcReadWriteSharedVars.containsKey(var.getName())) funcReadWriteSharedVars.get(var.getName()).add("R");
                                        else {
                                            Set<String> tmp = new HashSet<>();
                                            tmp.add("R");
                                            funcReadWriteSharedVars.put(var.getName(), tmp);
                                        }
                                    }
                                    for (Var var : gWVars) {
                                        if (funcReadWriteSharedVars.containsKey(var.getName())) funcReadWriteSharedVars.get(var.getName()).add("W");
                                        else {
                                            Set<String> tmp = new HashSet<>();
                                            tmp.add("W");
                                            funcReadWriteSharedVars.put(var.getName(), tmp);
                                        }
                                    }
                                }

                                waitlist.push(edge.getSuccessor());
                            }
                        }

                        visitedNodes.add(node);
                    }
                }

                results.put(func, funcReadWriteSharedVars);
            }
        }

        // iterate all the enable relation, we add the variables of corresponding interruption function to current interruption function.
        // 将对应中断函数的变量加入到当前中断函数中。
        return computeVarSetFixedPoint(intpEnableRelation, results);
    }

    /**
     * Compute the fixed-point of shared variable set. <br/>
     * e.g., interrupt enable relation: a -{enable}-> b, b -{enable}-> c <br/>
     * shared variables accessed by each interrupt: var_a:{v1, v2}, var_b:{v2, v3}, var_c:{v4} <br/>
     * => var'_a:{v1,v2,v3,v4}, var'_b:{v2,v3,v4}, var'_c:{v4}
     *
     * @param pIntpRelation The enable relation of interruptions.
     * @param pResults      The shared variables accessed by each interruption function.
     * @return The fixed-point of shared variable set.
     */
    private Map<String, Map<String, Set<String>>> computeVarSetFixedPoint(Map<String, Set<Integer>> pIntpRelation, Map<String, Map<String, Set<String>>> pResults) {
        ImmutableMap<String, Map<String, Set<String>>> constCopy = ImmutableMap.copyOf(pResults);  // <中断函数名，Set<变量>>
        Map<String, Map<String, Set<String>>> results = new HashMap<>(pResults);

        // iterate all the related interruption functions.
        for (Entry<String, Set<Integer>> intpEnRelation : pIntpRelation.entrySet()) {
            String intpFunc = intpEnRelation.getKey();
            Set<Integer> enIntpPriSet = intpEnRelation.getValue();

            // compute the fixed-point.
            Set<Integer> visitedPri = new HashSet<>();
            Iterator<Integer> intpRelatedPriIter = enIntpPriSet.iterator();
            Set<String> visitedIntpFunc = new HashSet<>();

            Deque<Integer> waitPriList = new ArrayDeque<>();
            waitPriList.addLast(intpRelatedPriIter.next());
            // process each priority.
            while (!waitPriList.isEmpty()) {
                int pri = waitPriList.removeFirst();

                if (visitedPri.contains(pri)) {
                    continue;
                }

                // filter the interruption function that its priority is equals to 'pri'.
                ImmutableSet<String> relIntpFuncs = from(priorityMap.keySet()).filter(s -> priorityMap.get(s) == pri).toSet();
                for (String func : relIntpFuncs) {
                    if (!visitedIntpFunc.contains(func)) {
                        // add the shared variables access by 'func'.
                        results.get(intpFunc).putAll(constCopy.get(func));
                        // check whether there are still exists certain priority that need to be processed.
                        Set<Integer> tmpPriSet = pIntpRelation.get(func);
                        if (tmpPriSet != null) {
                            waitPriList.addAll(tmpPriSet);
                        }

                        visitedIntpFunc.add(func);
                    }
                }
                visitedPri.add(pri);

                // we also need to add the next priority number.
                if (intpRelatedPriIter.hasNext()) {
                    waitPriList.add(intpRelatedPriIter.next());
                }
            }
        }

        return results;
    }


    private Map<String, Set<Integer>> addIntpEnableRelationForEdge(String pFunc,  // 函数名
                                                                   CFAEdge pEdge,  // 边
                                                                   Map<String, Set<Integer>> pIntpMap  // Map<函数名，Set<可插入中断函数的优先级>>返回状态
    ) {
        Map<String, Set<Integer>> results = new HashMap<>(pIntpMap);

        // we only process the interruption enable function. 只处理中断启用功能。
        if (pEdge instanceof AStatementEdge) {  // judge the edge must be a statement edge, i.e., if edge: "x=1" or "enable(1)", then the judge is true
            AStatement stmt = ((AStatementEdge) pEdge).getStatement();
            if (stmt instanceof AFunctionCall) {   // judge the edge must be a function call edge, i.e.judge the edge must be a function call edge
                String funcName = ((AFunctionCall) stmt).getFunctionCallExpression().getFunctionNameExpression().toString();
                if (!funcName.startsWith(enIntpFunc)) {  // the function must not begin with "enable_isr"
                    return results;
                }

                List<? extends AExpression> funcArgs = ((AFunctionCall) stmt).getFunctionCallExpression().getParameterExpressions();
                // obtain the enabled priority.
                if (funcArgs.size() == 1) {
                    AExpression enPriExp = funcArgs.get(0);
                    if (enPriExp instanceof CIntegerLiteralExpression) {
                        int enPri = ((CIntegerLiteralExpression) enPriExp).getValue().intValue();
                        if (!results.containsKey(pFunc)) {
                            results.put(pFunc, new HashSet<>());
                        }
                        results.get(pFunc).add(enPri);
                        return results;
                    }
                }
            }
        }

        return results;
    }

    private Integer getIntpEnablePriority(CFAEdge pEdge) {
        if (pEdge instanceof AStatementEdge) {
            AStatement stmt = ((AStatementEdge) pEdge).getStatement();
            if (stmt instanceof AFunctionCall) {   // judge the edge must be a function call edge, i.e.judge the edge must be a function call edge
                String funcName = ((AFunctionCall) stmt).getFunctionCallExpression().getFunctionNameExpression().toString();
//                assert funcName.startsWith(enIntpFunc);

                List<? extends AExpression> funcArgs = ((AFunctionCall) stmt).getFunctionCallExpression().getParameterExpressions();
                // obtain the enabled priority.
//                if (funcName.startsWith(enIntpFunc) && funcArgs.size() == 1) {
                if (funcArgs.size() == 1) {
                    AExpression enPriExp = funcArgs.get(0);
                    if (enPriExp instanceof CIntegerLiteralExpression) {
                        return ((CIntegerLiteralExpression) enPriExp).getValue().intValue();
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Integer> parseInterruptPriorityFile() {
        Map<String, Integer> results = new HashMap<>();
        String mainFunctionName = cfa.getMainFunction().getFunctionName();

        String priorityFile = priorityFileFolder  // 优先级目录
                + mainFunctionName.substring(0, mainFunctionName.indexOf("_main")) + priorityFileExtSuffix;  // 优先级后缀
        File priFile = new File(priorityFile);

        if (priFile.isFile() && priFile.exists()) {
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(priFile), "UTF-8"))) {
                Pattern p = Pattern.compile(priorityRegex);

                String line = "";
                while ((line = bfr.readLine()) != null) {
                    Matcher m = p.matcher(line);

                    if (m.find()) {
                        String isrFunc = m.group(1);
                        int priority = Integer.parseInt(m.group(2));

                        if (!results.containsKey(isrFunc)) {
                            results.put(isrFunc, priority);
                        } else {
                            logger.log(Level.WARNING, "Multiple interruption priority for the function: " + isrFunc);
                        }
                    } else {
                        logger.log(Level.WARNING, "Cannot extract the interruption information from line: " + line);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot read the priority file '" + priorityFile + "': " + e.getMessage());
            }
        } else {
            logger.log(Level.WARNING, "The priority file '" + priorityFile + "' for interruption does not exist!");
        }

        return results;
    }

//    private Map<String, CFAEdge> delayStrategyEdgeW = new HashMap<>();
//    private Map<String, CFAEdge> delayStrategyEdgeR = new HashMap<>();

    @Override
    public Collection<ThreadingIntpState> getAbstractSuccessorsForEdge(AbstractState pState, Precision precision, CFAEdge cfaEdge) throws CPATransferException, InterruptedException {
        Preconditions.checkNotNull(cfaEdge);   // 判空
//        delayStrategyEdgeR = new HashMap<>();
//        delayStrategyEdgeW = new HashMap<>();
        ThreadingIntpState state = (ThreadingIntpState) pState;
        ThreadingIntpState threadingState = exitThreads(state);   // clean the thread will exit

        EdgeVtx edgeVtx = (EdgeVtx) condDepGraph.getDGNode(cfaEdge.hashCode());
        if (!reachMainFunc) {
            reachMainFunc = cfaEdge.getFileLocation().equals(edgeInfo.getCfa().getMainFunction().getFileLocation());
        } else {
            Set<String> intpFunc = getcanIntpFunc(threadingState);
            threadingState = threadingState.updateRW(edgeVtx, cfaEdge, intpFunc);
        }

        final String activeThread = getActiveThread(cfaEdge, threadingState);  // The function is find a sentence of thread is the cfaEdge's prefix
        if (null == activeThread) {
            return ImmutableSet.of();
        }

        // check if atomic lock exists and is set for current thread
        if (useAtomicLocks && threadingState.hasLock(ATOMIC_LOCK) && !threadingState.hasLock(activeThread, ATOMIC_LOCK)) {
            return ImmutableSet.of();
        }

        // check if a local-access-lock allows to avoid exploration of some threads
        if (useLocalAccessLocks) {
            threadingState = handleLocalAccessLock(cfaEdge, threadingState, activeThread);
            if (threadingState == null) {
                return ImmutableSet.of();
            }
        }

        // check if currently is processing an interrupt.
        if (threadingState.isProcessingInterrupt()) {
            String curIntpId = threadingState.getButNotRemoveTopProcInterruptId();
            if (curIntpId != null && !activeThread.equals(curIntpId)) {
                return ImmutableSet.of();
            }
        }

        // check, if we can abort the complete analysis of all other threads after this edge.
        if (isEndOfMainFunction(cfaEdge) || isTerminatingEdge(cfaEdge)) {
            // VERIFIER_assume not only terminates the current thread, but the whole program
            return ImmutableSet.of();
        }

        // get all possible successors
        Collection<ThreadingIntpState> results = getAbstractSuccessorsFromWrappedCPAs(activeThread, threadingState, precision, cfaEdge);

        results = getAbstractSuccessorsForEdge0(cfaEdge, threadingState, activeThread, results);

        // Store the active thread in the given states, cf. JavaDoc of activeThread
        results = Collections2.transform(results, ts -> ts.withActiveThread(activeThread));

        // TODO: handle interruption.
        /* 相对于 threadingTransferRelation ，新增的对于中断函数的处理。 传入的 threadingState 为当前状态，
           results 为经过 threadingTransferRelation 处理后得到的后继状态集合(但是未经过中断处理)。
           最终返回的 results 为经过中断处理之后的后继状态集合。
        */


        String callFuncName = getFunctionCallName(cfaEdge);
        if (callFuncName != null && callFuncName.startsWith(enIntpFunc)) {
            int pLevel = getIntpEnablePriority(cfaEdge);
            if (pLevel == -1) {
//                if (!(threadingState.getDelayStrategyREdge().isEmpty() && threadingState.getDelayStrategyWEdge().isEmpty()))
                threadingState = threadingState.enableAllIntpAndCopy();

            } else
                threadingState = threadingState.enableIntpAndCopy(pLevel - 1);
        }

        results = handleInterruption(threadingState, results, cfaEdge);

        for (ThreadingIntpState threadingIntpState : results) {
            threadingIntpState.checkIntpisNull();
        }

        return ImmutableList.copyOf(results);
        // 到这里，新的中断开启完毕。中断开启后，后继状态( results 中的状态)中的调用栈和所
        // 处位置 被更新，同时当中还记录了新开启中断的优先级等信息。
    }

    private Set<String> getcanIntpFunc(ThreadingIntpState threadingIntpState) {
        Set<String> intpFunc = new HashSet<>();
        boolean[] intpLevelEnableFlags = threadingIntpState.getIntpLevelEnableFlags();
        for (int i = 0; i < intpLevelEnableFlags.length; i++) {
            if (intpLevelEnableFlags[i]) {
                for (String func : priorityMap.keySet()) {
                    if (priorityMap.get(func) == i + 1) {
                        intpFunc.add(func);
                    }
                }
            }
        }

        return intpFunc;
    }

    private Collection<ThreadingIntpState> updateRW(Collection<ThreadingIntpState> results, EdgeVtx edgeInfo, CFAEdge cfaEdge) {
        for (ThreadingIntpState threadingIntpState : results) {
            if (edgeInfo != null && !edgeInfo.getgReadVars().isEmpty()) {
                for (Var var : edgeInfo.getgReadVars()) {
                    Set<String> intpFunc = getcanIntpFunc(threadingIntpState);
                    threadingIntpState.setrEdge(var.getName(), cfaEdge, intpFunc);
                }
            }
            if (edgeInfo != null && !edgeInfo.getgWriteVars().isEmpty()) {
                for (Var var : edgeInfo.getgWriteVars()) {
                    Set<String> intpFunc = getcanIntpFunc(threadingIntpState);
                    threadingIntpState.setwEdge(var.getName(), cfaEdge, intpFunc);
                }
            }
        }
        return results;
    }


    /**
     * Search for the thread, where the current edge is available.
     * The result should be exactly one thread, that is denoted as 'active',
     * or NULL, if no active thread is available.
     * <p>
     * This method is needed, because we use the CompositeCPA to choose the edge,
     * and when we have several locations in the threadingState,
     * only one of them has an outgoing edge matching the current edge.
     */
    @Nullable
    private String getActiveThread(final CFAEdge cfaEdge, final ThreadingIntpState threadingState) {
        final Set<String> activeThreads = new HashSet<>();
        for (String id : threadingState.getThreadIds()) {
            if (Iterables.contains(threadingState.getThreadLocation(id).getOutgoingEdges(), cfaEdge)) { // the OutgoingEdges is cfaEdge, then this thread will activate
                activeThreads.add(id);
            }
        }

        if (isSingleThread && activeThreads.size() > 1) {   // Error : this mean if current cfaEdge have muti-prefix and we deal with the single thread now.
//      Preconditions.checkNotNull(threadingState.getKeepedActiveThread());
            assert threadingState.getKeptActiveThread() != null : "when verify the single thread program, " + "KeepedActiveThread shouldn't be null.";
            return threadingState.getKeptActiveThread();
        }

        assert activeThreads.size() <= 1 : "multiple active threads are not allowed: " + activeThreads;
        // then either the same function is called in different threads -> not supported.
        // (or CompositeCPA and ThreadingCPA do not work together)

        return activeThreads.isEmpty() ? null : Iterables.getOnlyElement(activeThreads);
    }

    /**
     * handle all edges related to thread-management:
     * THREAD_START, THREAD_JOIN, THREAD_EXIT, THREAD_MUTEX_LOCK, VERIFIER_ATOMIC,...
     * <p>
     * If nothing changes, then return <code>results</code> unmodified.
     */
    private Collection<ThreadingIntpState> getAbstractSuccessorsForEdge0(final CFAEdge cfaEdge, final ThreadingIntpState threadingState, final String activeThread, final Collection<ThreadingIntpState> results) throws UnrecognizedCodeException, InterruptedException {
        switch (cfaEdge.getEdgeType()) {
            case StatementEdge: {
                AStatement statement = ((AStatementEdge) cfaEdge).getStatement();
                if (statement instanceof AFunctionCall) {
                    AExpression functionNameExp = ((AFunctionCall) statement).getFunctionCallExpression().getFunctionNameExpression();
                    if (functionNameExp instanceof AIdExpression) {
                        final String functionName = ((AIdExpression) functionNameExp).getName();
                        switch (functionName) {
                            case THREAD_START:
                                return startNewThread(threadingState, statement, results, cfaEdge);
                            case THREAD_MUTEX_LOCK:
                                return addLock(threadingState, activeThread, extractLockId(statement), results);
                            case THREAD_MUTEX_UNLOCK:
                                return removeLock(activeThread, extractLockId(statement), results);
                            case THREAD_JOIN:
                                return joinThread(threadingState, statement, results);
                            case THREAD_EXIT:
                                // this function-call is already handled in the beginning with isLastNodeOfThread.
                                // return exitThread(threadingState, activeThread, results);
                                break;
                            case VERIFIER_ATOMIC_BEGIN:
                                if (useAtomicLocks) {
                                    return addLock(threadingState, activeThread, ATOMIC_LOCK, results);
                                }
                                break;
                            case VERIFIER_ATOMIC_END:
                                if (useAtomicLocks) {
                                    return removeLock(activeThread, ATOMIC_LOCK, results);
                                }
                                break;
                            default:
                                // nothing to do, return results
                        }

                        // special process for interrupt functions.
                        if (functionName.equals(disIntpFunc)) {
                            return handleDisEnInterruptFunc(results, (AFunctionCall) statement, false);
                        } else if (functionName.equals(enIntpFunc)) {
                            return handleDisEnInterruptFunc(results, (AFunctionCall) statement, true);
                        }
                    }
                }
                break;
            }
            case FunctionCallEdge: {
                if (useAtomicLocks) {
                    // cloning changes the function-name -> we use 'startsWith'.
                    // we have 2 different atomic sequences:
                    //   1) from calling VERIFIER_ATOMIC_BEGIN to exiting VERIFIER_ATOMIC_END.
                    //      (@Deprecated, for old benchmark tasks)
                    //   2) from calling VERIFIER_ATOMIC_X to exiting VERIFIER_ATOMIC_X where X can be anything
                    final String calledFunction = cfaEdge.getSuccessor().getFunctionName();
                    if (calledFunction.startsWith(VERIFIER_ATOMIC_BEGIN)) {
                        return addLock(threadingState, activeThread, ATOMIC_LOCK, results);
                    } else if (calledFunction.startsWith(VERIFIER_ATOMIC) && !calledFunction.startsWith(VERIFIER_ATOMIC_END)) {
                        return addLock(threadingState, activeThread, ATOMIC_LOCK, results);
                    }
                }
                break;
            }
            case FunctionReturnEdge: {
                if (useAtomicLocks) {
                    // cloning changes the function-name -> we use 'startsWith'.
                    // we have 2 different atomic sequences:
                    //   1) from calling VERIFIER_ATOMIC_BEGIN to exiting VERIFIER_ATOMIC_END.
                    //      (@Deprecated, for old benchmark tasks)
                    //   2) from calling VERIFIER_ATOMIC_X to exiting VERIFIER_ATOMIC_X  where X can be anything
                    final String exitedFunction = cfaEdge.getPredecessor().getFunctionName();
                    if (exitedFunction.startsWith(VERIFIER_ATOMIC_END)) {
                        return removeLock(activeThread, ATOMIC_LOCK, results);
                    } else if (exitedFunction.startsWith(VERIFIER_ATOMIC) && !exitedFunction.startsWith(VERIFIER_ATOMIC_BEGIN)) {
                        return removeLock(activeThread, ATOMIC_LOCK, results);
                    }
                }
                break;
            }
            default:
                // nothing to do
        }
        return results;
    }

    /**
     * compute successors for the current edge.
     * There will be only one successor in most cases, even with N threads,
     * because the edge limits the transitions to only one thread,
     * because the LocationTransferRelation will only find one succeeding CFAnode.
     */
    private Collection<ThreadingIntpState> getAbstractSuccessorsFromWrappedCPAs(String activeThread, ThreadingIntpState threadingState, Precision precision, CFAEdge cfaEdge) throws CPATransferException, InterruptedException {

        // compute new locations
        Collection<? extends AbstractState> newLocs = locationCPA.getTransferRelation().getAbstractSuccessorsForEdge(threadingState.getThreadLocation(activeThread), precision, cfaEdge);

        // compute new stacks
        Collection<? extends AbstractState> newStacks = callstackCPA.getTransferRelation().getAbstractSuccessorsForEdge(threadingState.getThreadCallstack(activeThread), precision, cfaEdge);

        // combine them pairwise, all combinations needed
        final Collection<ThreadingIntpState> results = new ArrayList<>();
        for (AbstractState loc : newLocs) {
            for (AbstractState stack : newStacks) {
                results.add(threadingState.updateLocationAndCopy(activeThread, stack, loc));
            }
        }

        return results;
    }

    private Collection<ThreadingIntpState> handleDisEnInterruptFunc(final Collection<ThreadingIntpState> results, final AFunctionCall stmt, boolean enableIntp) throws UnrecognizedCodeException {
        List<? extends AExpression> param = stmt.getFunctionCallExpression().getParameterExpressions();
        if (param.size() != 1) {
            throw new UnrecognizedCodeException("unsupported number of parameters of enable/disable interruption " + "function", stmt);
        }

        AExpression opIntpLevelExp = param.get(0);
        if (opIntpLevelExp instanceof CIntegerLiteralExpression) {
            int opIntpLevel = ((CIntegerLiteralExpression) opIntpLevelExp).getValue().intValue();

            if (opIntpLevel >= -1) {
                if (enableIntp) {
                    if (opIntpLevel == -1) {
                        return transform(results, ts -> ts.enableAllIntpAndCopy());
                    } else {
                        // the interrupt number should minus one to fit the array index.
                        return transform(results, ts -> ts.enableIntpAndCopy(opIntpLevel - 1));
                    }
                } else {
                    if (opIntpLevel == -1) {
                        return transform(results, ts -> ts.disableAllIntpAndCopy());
                    } else {
                        // the interrupt number should minus one to fit the array index.
                        return transform(results, ts -> ts.disableIntpAndCopy(opIntpLevel - 1));
                    }
                }
            } else {
                throw new UnrecognizedCodeException("unsupported interrupt level", opIntpLevelExp);
            }
        } else {
            throw new UnrecognizedCodeException("unsupported function argument", opIntpLevelExp);
        }
    }

    /**
     * checks whether the location is the last node of a thread,
     * i.e. the current thread will terminate after this node.
     */
    public static boolean isLastNodeOfThread(CFANode node) {

        if (0 == node.getNumLeavingEdges()) {    // 出边
            return true;
        }

        if (1 == node.getNumEnteringEdges()) {   // 入边
            return isThreadExit(node.getEnteringEdge(0));  // judge the edge is or not meaning exit
        }

        return false;
    }

    private static boolean isThreadExit(CFAEdge cfaEdge) {
        if (CFAEdgeType.StatementEdge == cfaEdge.getEdgeType()) {
            AStatement statement = ((AStatementEdge) cfaEdge).getStatement();
            if (statement instanceof AFunctionCall) {    // judge the edge must be a function call edge, i.e. if edge is "enable" or "disable",
                // then it's
                AExpression functionNameExp = ((AFunctionCall) statement).getFunctionCallExpression().getFunctionNameExpression();
                if (functionNameExp instanceof AIdExpression) {
                    return THREAD_EXIT.equals(((AIdExpression) functionNameExp).getName());
                }
            }
        }
        return false;
    }

    /**
     * the whole program will terminate after this edge
     */
    private static boolean isTerminatingEdge(CFAEdge edge) {
        return edge.getSuccessor() instanceof CFATerminationNode;
    }

    /**
     * the whole program will terminate after this edge
     */
    private boolean isEndOfMainFunction(CFAEdge edge) {
        return Objects.equals(cfa.getMainFunction().getExitNode(), edge.getSuccessor());
    }

    private ThreadingIntpState exitThreads(ThreadingIntpState tmp) {
        // clean up exited threads.
        // this is done before applying any other step.
        for (String id : tmp.getThreadIds()) {
            if (isLastNodeOfThread(tmp.getThreadLocation(id).getLocationNode())) {   // 是否是某线程最后一个节点
                // remove interrupt-id from the 'intpStack'.
                if (tmp.isInterruptId(id)) {    // judge the current thread id is not the MAX_PRIORITY_NUMBER and MIN_PRIORITY_NUMBER
                    tmp.removeIntpFromStack(id);  // remove the current thread-id from intpStack
                }

                // then, we remove the thread-id from current state.
                tmp = removeThreadId(tmp, id);
            }
        }

        return tmp;
    }

    /**
     * remove the thread-id from the state, and cleanup remaining locks of this thread.
     */
    private ThreadingIntpState removeThreadId(ThreadingIntpState ts, final String id) {
        if (useLocalAccessLocks) {
            ts = ts.removeLockAndCopy(id, LOCAL_ACCESS_LOCK);
        }
        if (ts.hasLockForThread(id)) {
            logger.log(Level.WARNING, "dying thread", id, "has remaining locks in state", ts);
        }
        return ts.removeThreadAndCopy(id);
    }

    private Collection<ThreadingIntpState> startNewThread(final ThreadingIntpState threadingState, final AStatement statement, final Collection<ThreadingIntpState> results, final CFAEdge cfaEdge) throws UnrecognizedCodeException, InterruptedException {

        // first check for some possible errors and unsupported parts
        List<? extends AExpression> params = ((AFunctionCall) statement).getFunctionCallExpression().getParameterExpressions();
        if (!(params.get(0) instanceof CUnaryExpression)) {
            throw new UnrecognizedCodeException("unsupported thread assignment", params.get(0));
        }
        if (!(params.get(2) instanceof CUnaryExpression)) {
            throw new UnrecognizedCodeException("unsupported thread function call", params.get(2));
        }
        CExpression expr0 = ((CUnaryExpression) params.get(0)).getOperand();
        CExpression expr2 = ((CUnaryExpression) params.get(2)).getOperand();
        if (!(expr0 instanceof CIdExpression)) {
            throw new UnrecognizedCodeException("unsupported thread assignment", expr0);
        }
        if (!(expr2 instanceof CIdExpression)) {
            throw new UnrecognizedCodeException("unsupported thread function call", expr2);
        }
        if (!(params.get(3) instanceof CExpression)) {
            throw new UnrecognizedCodeException("unsupported thread function argument", params.get(3));
        }

        // now create the thread
        CIdExpression id = (CIdExpression) expr0;
        CIdExpression function = (CIdExpression) expr2;
        CExpression threadArg = (CExpression) params.get(3);

        if (useAllPossibleClones) {
            // for witness validation we need to produce all possible successors,
            // the witness automaton should then limit their number by checking function-entries..
            final Collection<ThreadingIntpState> newResults = new ArrayList<>();
            Set<Integer> usedNumbers = threadingState.getThreadNums();
            for (int i = ThreadingIntpState.MIN_THREAD_NUM; i < maxNumberOfThreads; i++) {
                if (!usedNumbers.contains(i)) {
                    newResults.addAll(createThreadWithNumber(threadingState, id, cfaEdge, function, threadArg, i, results));
                }
            }
            return newResults;

        } else {
            // a default reachability analysis can determine the thread-number on its own.
            int newThreadNum = getNewThreadNum(threadingState, function.getName());
            return createThreadWithNumber(threadingState, id, cfaEdge, function, threadArg, newThreadNum, results);
        }
    }

    private int getNewThreadNum(final ThreadingIntpState threadingState, final String function) {
        if (useIncClonedFunc) {
            return threadingState.getNewThreadNum(function);
        } else {
            return threadingState.getSmallestMissingThreadNum();
        }
    }

    private Collection<ThreadingIntpState> createThreadWithNumber(final ThreadingIntpState threadingState, final CIdExpression id, final CFAEdge cfaEdge, final CIdExpression function, final CExpression threadArg, final int newThreadNum, final Collection<ThreadingIntpState> results) throws UnrecognizedCodeException, InterruptedException {

        String functionName = function.getName();
        int pri = priorityMap.containsKey(functionName) ? priorityMap.get(functionName) : (intpPriOrder.equals(InterruptPriorityOrder.BH) ? ThreadingIntpState.MIN_PRIORITY_NUMBER : ThreadingIntpState.MAX_PRIORITY_NUMBER);
        if (useClonedFunctions) {
            functionName = CFACloner.getFunctionName(functionName, newThreadNum);
        }

        String threadId = getNewThreadId(threadingState, id.getName());

        // update all successors with a new started thread
        final Collection<ThreadingIntpState> newResults = new ArrayList<>();
        for (ThreadingIntpState ts : results) {
            ThreadingIntpState newThreadingState = addNewThread(ts, threadId, pri, newThreadNum, functionName);
            if (null != newThreadingState) {
                // create a function call for the thread creation
                CFunctionCallEdge functionCall = createThreadEntryFunctionCall(cfaEdge, function.getExpressionType(), functionName, threadArg);
                newResults.add(newThreadingState.withEntryFunction(functionCall));
            }
        }
        return newResults;
    }

    /**
     * Create a functioncall expression for the thread creation.
     *
     * <p>
     * For `pthread_create(t, ?, foo, arg)` with we return `foo(arg)`.
     *
     * @param cfaEdge      where pthread_create was called
     * @param type         return-type of the called function
     * @param functionName the (maybe indexed) name of the called function
     * @param arg          the argument given to the called function
     */
    private CFunctionCallEdge createThreadEntryFunctionCall(final CFAEdge cfaEdge, final CType type, final String functionName, final CExpression arg) {
        CFunctionEntryNode functioncallNode = (CFunctionEntryNode) Preconditions.checkNotNull(cfa.getFunctionHead(functionName), "Function '" + functionName + "' was not found. Please enable cloning for the CFA!");
        CFunctionDeclaration functionDeclaration = (CFunctionDeclaration) functioncallNode.getFunction();
        CIdExpression functionId = new CIdExpression(cfaEdge.getFileLocation(), type, functionName, functionDeclaration);
        CFunctionCallExpression functionCallExpr = new CFunctionCallExpression(cfaEdge.getFileLocation(), type, functionId, arg != null ? ImmutableList.of(arg) : ImmutableList.of(), functionDeclaration);
        CFunctionCall functionCall = new CFunctionCallStatement(cfaEdge.getFileLocation(), functionCallExpr);
        CFunctionCallEdge edge = new CFunctionCallEdge(functionCallExpr.toASTString(), cfaEdge.getFileLocation(), cfaEdge.getSuccessor(), functioncallNode, functionCall, null);
        return edge;
    }

    /**
     * returns a new state with a new thread added to the given state.
     *
     * @param threadingState the previous state where to add the new thread
     * @param threadId       a unique identifier for the new thread
     * @param newThreadNum   a unique number for the new thread
     * @param functionName   the main-function of the new thread
     * @return a threadingState with the new thread,
     * or {@code null} if the new thread cannot be created.
     */
    @Nullable ThreadingIntpState addNewThread(ThreadingIntpState threadingState, String threadId, int pri, int newThreadNum, String functionName) throws InterruptedException {
        CFANode functioncallNode = Preconditions.checkNotNull(cfa.getFunctionHead(functionName), "Function '" + functionName + "' was not found. " + "Please enable cloning for the CFA!");
        AbstractState initialStack = callstackCPA.getInitialState(functioncallNode, StateSpacePartition.getDefaultPartition());
        AbstractState initialLoc = locationCPA.getInitialState(functioncallNode, StateSpacePartition.getDefaultPartition());

        if (maxNumberOfThreads == -1 || threadingState.getThreadIds().size() < maxNumberOfThreads) {
            // 根据传入的中断函数名来获取函数的入口点( cfa.getFunctionHead() )。根据函 数入口
            // 点获取到对应的调用栈 initialStack 和位置 initialLoc 。
            threadingState = threadingState.addThreadAndCopy(threadId, newThreadNum, pri, initialStack, initialLoc);
            return threadingState;
        } else {
            logger.log(Level.WARNING, "number of threads is limited, cannot create thread %s", threadId);
            return null;
        }
    }

    /**
     * returns the threadId if possible, else the next indexed threadId.
     */
    private String getNewThreadId(final ThreadingIntpState threadingState, final String threadId) throws UnrecognizedCodeException {
        if (!allowMultipleLHS && threadingState.getThreadIds().contains(threadId)) {
            throw new UnrecognizedCodeException("multiple thread assignments to same LHS not supported: " + threadId, null, null);
        }
        String newThreadId = threadId;
        int index = 0;
        while (threadingState.getThreadIds().contains(newThreadId) && (maxNumberOfThreads == -1 || index < maxNumberOfThreads)) {
            index++;
            newThreadId = threadId + THREAD_ID_SEPARATOR + index;
            logger.log(Level.WARNING, "multiple thread assignments to same LHS, " + "using identifier %s instead of " + "%s", newThreadId, threadId);
        }
        return newThreadId;
    }

    private Collection<ThreadingIntpState> addLock(final ThreadingIntpState threadingState, final String activeThread, String lockId, final Collection<ThreadingIntpState> results) {
        if (threadingState.hasLock(lockId)) {
            // some thread (including activeThread) has the lock, using it twice is not possible
            return ImmutableSet.of();
        }

        return transform(results, ts -> ts.addLockAndCopy(activeThread, lockId));
    }

    /**
     * get the name (lockId) of the new lock at the given edge, or NULL if no lock is required.
     */
    static @Nullable String getLockId(final CFAEdge cfaEdge) throws UnrecognizedCodeException {
        if (cfaEdge.getEdgeType() == CFAEdgeType.StatementEdge) {
            final AStatement statement = ((AStatementEdge) cfaEdge).getStatement();
            if (statement instanceof AFunctionCall) {
                final AExpression functionNameExp = ((AFunctionCall) statement).getFunctionCallExpression().getFunctionNameExpression();
                if (functionNameExp instanceof AIdExpression) {
                    final String functionName = ((AIdExpression) functionNameExp).getName();
                    if (THREAD_MUTEX_LOCK.equals(functionName)) {
                        return extractLockId(statement);
                    }
                }
            }
        }
        // otherwise no lock is required
        return null;
    }

    private static String extractLockId(final AStatement statement) throws UnrecognizedCodeException {
        // first check for some possible errors and unsupported parts
        List<? extends AExpression> params = ((AFunctionCall) statement).getFunctionCallExpression().getParameterExpressions();
        if (!(params.get(0) instanceof CUnaryExpression)) {
            throw new UnrecognizedCodeException("unsupported thread locking", params.get(0));
        }
//  CExpression expr0 = ((CUnaryExpression)params.get(0)).getOperand();
//  if (!(expr0 instanceof CIdExpression)) {
//    throw new UnrecognizedCodeException("unsupported thread lock assignment", expr0);
//  }
//  String lockId = ((CIdExpression) expr0).getName();

        String lockId = ((CUnaryExpression) params.get(0)).getOperand().toString();
        return lockId;
    }

    private Collection<ThreadingIntpState> removeLock(final String activeThread, final String lockId, final Collection<ThreadingIntpState> results) {
        return transform(results, ts -> ts.removeLockAndCopy(activeThread, lockId));
    }

    private Collection<ThreadingIntpState> joinThread(ThreadingIntpState threadingState, AStatement statement, Collection<ThreadingIntpState> results) throws UnrecognizedCodeException {

        if (threadingState.getThreadIds().contains(extractParamName(statement, 0))) {
            // we wait for an active thread -> nothing to do
            return ImmutableSet.of();
        }

        return results;
    }

    /**
     * extract the name of the n-th parameter from a function call.
     */
    static String extractParamName(AStatement statement, int n) throws UnrecognizedCodeException {
        // first check for some possible errors and unsupported parts
        List<? extends AExpression> params = ((AFunctionCall) statement).getFunctionCallExpression().getParameterExpressions();
        AExpression expr = params.get(n);
        if (!(expr instanceof CIdExpression)) {
            throw new UnrecognizedCodeException("unsupported thread join access", expr);
        }

        return ((CIdExpression) expr).getName();
    }

    /**
     * optimization for interleaved threads.
     * When a thread only accesses local variables, we ignore other threads
     * and add an internal 'atomic' lock.
     *
     * @return updated state if possible, else NULL.
     */
    private @Nullable ThreadingIntpState handleLocalAccessLock(CFAEdge cfaEdge, final ThreadingIntpState threadingState, String activeThread) {

        // check if local access lock exists and is set for current thread
        if (threadingState.hasLock(LOCAL_ACCESS_LOCK) && !threadingState.hasLock(activeThread, LOCAL_ACCESS_LOCK)) {
            return null;
        }

        // add local access lock, if necessary and possible
        final boolean isImporantForThreading = globalAccessChecker.hasGlobalAccess(cfaEdge) || isImporantForThreading(cfaEdge);
        if (isImporantForThreading) {
            return threadingState.removeLockAndCopy(activeThread, LOCAL_ACCESS_LOCK);
        } else {
            return threadingState.addLockAndCopy(activeThread, LOCAL_ACCESS_LOCK);
        }
    }

    private static boolean isImporantForThreading(CFAEdge cfaEdge) {
        switch (cfaEdge.getEdgeType()) {
            case StatementEdge: {
                AStatement statement = ((AStatementEdge) cfaEdge).getStatement();
                if (statement instanceof AFunctionCall) {
                    AExpression functionNameExp = ((AFunctionCall) statement).getFunctionCallExpression().getFunctionNameExpression();
                    if (functionNameExp instanceof AIdExpression) {
                        return THREAD_FUNCTIONS.contains(((AIdExpression) functionNameExp).getName());
                    }
                }
                return false;
            }
            case FunctionCallEdge:
                // @Deprecated, for old benchmark tasks
                return cfaEdge.getSuccessor().getFunctionName().startsWith(VERIFIER_ATOMIC_BEGIN);
            case FunctionReturnEdge:
                // @Deprecated, for old benchmark tasks
                return cfaEdge.getPredecessor().getFunctionName().startsWith(VERIFIER_ATOMIC_END);
            default:
                return false;
        }
    }

    @Override
    public Collection<? extends AbstractState> strengthen(AbstractState state, Iterable<AbstractState> otherStates, @Nullable CFAEdge cfaEdge, Precision precision) throws CPATransferException, InterruptedException {
        Optional<ThreadingIntpState> results = Optional.of((ThreadingIntpState) state);

        for (AutomatonState automatonState : AbstractStates.projectToType(otherStates, AutomatonState.class)) {
            if ("WitnessAutomaton".equals(automatonState.getOwningAutomatonName())) {
                results = results.map(ts -> handleWitnessAutomaton(ts, automatonState));
            }
        }

        // modified by yzc: 22-08-05
        if (isSingleThread) {
            return Optionals.asSet(results.map(ts -> {
                String keepedActiveThread = ts.getIntpStack().isEmpty() ? ts.getActiveThread() : ts.getIntpStack().peekLast();
                return ts.withActiveThread(null).withEntryFunction(null).withKeptActiveThread(keepedActiveThread);
            }));
        } else {
            return Optionals.asSet(results.map(ts -> ts.withActiveThread(null).withEntryFunction(null)));
        }

        // delete temporary information from the state, cf. JavaDoc of the called methods
//    return Optionals.asSet(results.map(ts -> ts.withActiveThread(null).withEntryFunction(null)));
    }

    private @Nullable ThreadingIntpState handleWitnessAutomaton(ThreadingIntpState ts, AutomatonState automatonState) {
        Map<String, AutomatonVariable> vars = automatonState.getVars();
        AutomatonVariable witnessThreadId = vars.get(KeyDef.THREADID.toString().toUpperCase());
        String threadId = ts.getActiveThread();
        if (witnessThreadId == null || threadId == null || witnessThreadId.getValue() == 0) {
            // values not available or default value zero -> ignore and return state unchanged
            return ts;
        }

        Integer witnessId = ts.getThreadIdForWitness(threadId);
        if (witnessId == null) {
            if (ts.hasWitnessIdForThread(witnessThreadId.getValue())) {
                // state contains a mapping, but not for current thread -> wrong branch?
                // TODO returning NULL here would be nice, but leads to unaccepted witnesses :-(
                return ts;
            } else {
                // we know nothing, but can store the new mapping in the state
                return ts.setThreadIdForWitness(threadId, witnessThreadId.getValue());
            }
        }
        if (witnessId.equals(witnessThreadId.getValue())) {
            // corrent branch
            return ts;
        } else {
            // threadId does not match -> no successor
            return ts;
        }
    }

    /**
     * if the current edge creates a new function, return its name, else nothing.
     */
    public static Optional<String> getCreatedThreadFunction(final CFAEdge edge) throws UnrecognizedCodeException {
        if (edge instanceof AStatementEdge) {
            AStatement statement = ((AStatementEdge) edge).getStatement();
            if (statement instanceof AFunctionCall) {
                AExpression functionNameExp = ((AFunctionCall) statement).getFunctionCallExpression().getFunctionNameExpression();
                if (functionNameExp instanceof AIdExpression) {
                    final String functionName = ((AIdExpression) functionNameExp).getName();
                    if (ThreadingIntpTransferRelation.THREAD_START.equals(functionName)) {
                        List<? extends AExpression> params = ((AFunctionCall) statement).getFunctionCallExpression().getParameterExpressions();
                        if (!(params.get(2) instanceof CUnaryExpression)) {
                            throw new UnrecognizedCodeException("unsupported thread function call", params.get(2));
                        }
                        CExpression expr2 = ((CUnaryExpression) params.get(2)).getOperand();
                        if (!(expr2 instanceof CIdExpression)) {
                            throw new UnrecognizedCodeException("unsupported thread function call", expr2);
                        }
                        String newThreadFunctionName = ((CIdExpression) expr2).getName();
                        return Optional.of(newThreadFunctionName);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Collection<ThreadingIntpState> handleInterruption(final ThreadingIntpState threadingState, final Collection<ThreadingIntpState> results, CFAEdge cfaEdge) throws UnrecognizedCodeException, InterruptedException {
        // first step: obtain all the interrupt point to check whether this location need to be
        // interrupted. 获取所有中断点
        Collection<Pair<CFANode, String>> intpPoints = obtainInterruptPoints(threadingState, cfaEdge);
        /*
        首先，从 threadingState 中获取所有的中断点。利用 obtainInterruptPoints, 遍历 threadingState 中的所有位置，
        如果某个位置( loc )是代表性选择点，则获取 loc 对应的所有中断函数，并为每个函数生成一个 <loc, 中断函数名> 的 pair。
         */

        if (!intpPoints.isEmpty() && !threadingState.isAllInterruptDisabled()) {
            // 只有两个条件均成立时，才能进行中断相关的处理：1） threadingStaet 中有中断点。2）存在中断函数处于 enable 状态。
            // second step: we need to filter out some invalid interrupts by using the following rules:
            // 利用一定的规则去除无效的中断：
            Set<Pair<Integer, String>> canIntpPoints = filterOutInvalidInterruptPoints(threadingState, intpPoints);
            /* 到此为止， canIntpPoints 包含了将要触发的中断函数及其对应的优先级。 */

            // third step: group and re-order these interrupt points according to their priorities.
            /* 这里将 canIntpPoints 中的中断函数按照优先级形成不同的列表(每个中断函数被放在一个列表中)，得到列表集合 orderedIntpPoints 。*/
            Set<List<String>> orderedIntpPoints = createInteruptCreationSet(canIntpPoints);

            // fourth step: create interrupt threads for these points.
            // 根据 orderedIntpPoints 中的中断函数列表来开启中断
            return createInterruptThreads(orderedIntpPoints, results);
        } else {
            // do nothing if no interrupt point exists or all the interrupt are disabled.
            return results;
        }
    }

    /**
     * This function filter out some invalid interrupts by using the following rules:
     * <p>
     * 1. replicated interrupts that belongs to different selection points; <br/>
     * 2. the priority a interrupt in 'intpPoints' is smaller or equal to that of the current
     * processing interrupt (i.e., the top element of 'intpStack'); <br/>
     * 3. the interrupt i is disabled (i.e., intpLevelEnableFlags[i] == false); <br/>
     * 4. re-interrupt the same interrupts (i.e., interrupt reentrant is not supported) (this rule
     * will be covered by rule 2); <br/>
     * 5. the bound of interrupt-times of corresponding interrupt is reached; <br/>
     * </p>
     *
     * @param threadingState Current threading state.
     * @param intpPoints     The interrupts that can occur at current location.
     * @return each pair contains: 1) the priority of the interrupt, and 2) the name of interrupt.
     */
    private Set<Pair<Integer, String>> filterOutInvalidInterruptPoints(final ThreadingIntpState threadingState, final Collection<Pair<CFANode, String>> intpPoints) {
        //// rule 1: remove replicated interrupts, and add priority for last interrupts.
        /* 首先从 intpPoints 中取出所有的中断函数组成集合。然后遍历集合中的中断函数，并从 priorityMap 中取出相应优先级形成 pair： <优先级, 中断函数> 。*/
        ImmutableSet<String> intpFuncSet = from(intpPoints).transform(p -> p.getSecond()).toSet();
        ImmutableSet<Pair<Integer, String>> intpPriFuncPairSet = from(intpFuncSet).transform(i -> Pair.of(priorityMap.get(i), i)).toSet();

        //// rule 2: remove all the interrupts that their interrupt-priority is smaller or equal to that
        //// of the current processing interrupt.
        // get the top element of current processing interrupt.
        /* 过滤掉优先级小于等于当前处理的函数的优先级的中断函数。 当前处理的函数的优先级通过取出 intpStack 中最后一个元素得到。如果 intpStack 为空，则当前处理函数的优先级取最低优先级。*/
        ImmutableSet<Pair<Integer, String>> intpsPriGreater = getGreaterPriorityInterrupts(threadingState, intpPriFuncPairSet);

        //// rule 3: removes disabled interrupts.
        /*移除被 disable 的中断。数组 intpLevelEnableFlags 保存了关于各优先级中断开关的情况。
        遍历 intpsPriGreater 中的中断函数时，利用中断函数对应的优先级在 intpLevelEnableFlags 中查询中断函数的开关状态。
        只有处于开启状态的中断函数被保留，存放到 enIntpsGreater 。*/
        ImmutableSet<Pair<Integer, String>> enIntpsPriGreater = from(intpsPriGreater).filter(i -> threadingState.isInterruptEnabled(i.getFirst())).toSet();
        // ImmutableSet<String> enIntpsPriGreaterFuncNameSet =
        // from(enIntpsPriGreater).transform(i -> i.getSecond()).toSet();


        //// rule 5: remove the time interrupts that reach the bound of interrupt-times.
        /*
         * 如果每个中断函数触发的次数受到限制( maxInterruptTimesForEachFunc != -1 )， 则过滤掉那些已经达到触发次数限制的中断函数。
         * getCurrentInterruptTimes() 利用 intpTimes 查询中断函数的触发次数，中断函数的触发次数保存在 intpTimes 中。
         * */
        if (maxInterruptTimesForEachFunc != -1) {
            return from(enIntpsPriGreater).filter(i -> threadingState.getCurrentInterruptTimes(i.getSecond()) < maxInterruptTimesForEachFunc).toSet();
        } else {
            return intpsPriGreater;
        }
    }

    private ImmutableSet<Pair<Integer, String>> getGreaterPriorityInterrupts(final ThreadingIntpState threadingState, final ImmutableSet<Pair<Integer, String>> intpPriFuncPairSet) {
        String topIntpId = threadingState.getButNotRemoveTopProcInterruptId();
        int topIntpPri = topIntpId != null ? priorityMap.get(topIntpId) : getLowestPriority();

        ImmutableSet<Pair<Integer, String>> intpsPriGreater;
        if (intpPriOrder.equals(InterruptPriorityOrder.BH)) {
            intpsPriGreater = from(intpPriFuncPairSet).filter(p -> p.getFirst() > topIntpPri).toSet();
        } else {
            intpsPriGreater = from(intpPriFuncPairSet).filter(p -> p.getFirst() < topIntpPri).toSet();
        }

        return intpsPriGreater;
    }

    private Set<List<String>> createInteruptCreationSet(final Set<Pair<Integer, String>> canIntpPoints) {
        Set<List<String>> results = new HashSet<>();

        // first step: add an empty list, it means that no interruption needs to be executed.
        results.add(new ArrayList<>());

        // second step: for each interruption point, we create a list that only contains that interruption.
        canIntpPoints.forEach(i -> results.add(List.of(i.getSecond())));

        return results;
    }

    private Set<List<String>> orderInterruptPoints(final Set<Pair<Integer, String>> canIntpPoints) {
        // first step: create the power set of these interrupts.
        Set<Set<Pair<Integer, String>>> intpsPowerSet = Sets.powerSet(canIntpPoints);

        // second step: re-order these interrupts by their priority.
        Set<List<String>> results = new HashSet<>();
        intpsPowerSet.forEach(s -> {
            // sort the set s.
            List<Pair<Integer, String>> toList = Lists.newArrayList(s.iterator());
            toList.sort(new Comparator<Pair<Integer, String>>() {

                @Override
                public int compare(Pair<Integer, String> pO1, Pair<Integer, String> pO2) {
                    if (intpPriOrder.equals(InterruptPriorityOrder.BH)) {
                        return pO2.getFirst() - pO1.getFirst();
                    } else {
                        return pO1.getFirst() - pO2.getFirst();
                    }
                }
            });
            // only preserve interrupt functions.
            results.add(from(toList).transform(p -> p.getSecond()).toList());
        });

        return results;
    }

    private Collection<ThreadingIntpState> createInterruptThreads(final Set<List<String>> orderedIntpPoints, final Collection<ThreadingIntpState> results) throws UnrecognizedCodeException, InterruptedException {
        // 注意，这里的 results 是经过 threadingTransferRelation 处理之后的后继状态集
        // 合。 createInterruptThreads 中通过遍历该集合来进行中断处理(即为 results 中的所
        // 有状态都进行中断处理)。在 createInterruptThreads 中，对与 results 中的每一个状态 ts ，遍历
        // orderedIntpPoints 中的所有列表，开启相应的中断(不同的中断放在不同的列表里)。 chulihou
        Collection<ThreadingIntpState> newResults = new ArrayList<>();

        // add new interrupts for each results.
        for (ThreadingIntpState ts : results) {
            // iterate over the orderedIntpPoints to create new interrupts.
            Iterator<List<String>> oip = orderedIntpPoints.iterator();
            while (oip.hasNext()) {
                ThreadingIntpState newThreadingState = ts;
                List<String> oil = oip.next();
                // create interrupts orderly.
                for (int i = 0; i < oil.size(); ++i) {
                    String intpFunc = oil.get(i);
                    // 在触发中断之前，还需要判断是否达到中断嵌套次数：
                    if ((ts.getIntpStackLevel() < maxLevelInterruptNesting) && (intpFunc != null && !intpFunc.isEmpty())) {
                        // 中断嵌套次数根据 intpStack 的大小来判断， maxLevelInterruptNesting 是可配变
                        // 量，可在配置文件中设置。若满足要求，则为 ts 开启中断：
                        newThreadingState = addNewIntpThread(newThreadingState, intpFunc);
                    } else {
                        // we do nothing if the maximum interrupt nesting level is reached.
                        logger.log(Level.WARNING, "current state reaches the maximum interrupt nesting level " + maxLevelInterruptNesting);
                    }
                }
                newResults.add(newThreadingState);
            }
        }

        return newResults;
    }

    private ThreadingIntpState addNewIntpThread(final ThreadingIntpState threadingState, final String intpFunc) throws UnrecognizedCodeException, InterruptedException {
        // 开启中断– addNewIntpThread()
        // get the priority of this interrupt function.
        Preconditions.checkArgument(priorityMap.containsKey(intpFunc), "the priority of interrupt function '" + intpFunc + "' is unknown");
        int intpFuncPri = priorityMap.get(intpFunc);

        // get the cloned name of this interrupt function.
        String intpFuncName = intpFunc;
        int newIntpNum = getNewThreadNum(threadingState, intpFunc);
        if (isVerifyingConcurrentProgram && useClonedFunctions) {
            intpFuncName = CFACloner.getFunctionName(intpFuncName, newIntpNum);
        }

        String intpThreadId = getNewThreadId(threadingState, intpFunc);

        // create new thread for the interrupt.  采用类似线程创建的方式来开启中断函数：
        // intpThreadId 对应线程ID， intpFuncPri 为中断优先级， newIntpNum 变量用来区分不同的克隆函数， intpFuncName 为中断函数名，用来对应线程函数名。
        ThreadingIntpState resThreadingState = addNewThread(threadingState, intpThreadId, intpFuncPri, newIntpNum, intpFuncName);

        if (resThreadingState != null) {
            // setup other information.
            // 开启中断之后，被开启的中断需要添加到 intpStack 中(添加到末尾)，这里使
            // 用 intpThreadId 而不是 intpFuncName ，是为了区分相同中断函数的不同调用(多线程中
            // 需要用线程ID来区分相同线程函数的不同次创建)。同时被开启的中断函数触发次数+1
            resThreadingState.pushIntpStack(intpThreadId);
            resThreadingState.addIntpFuncTimes(intpFunc);
        }

        return resThreadingState;
    }

    private Collection<Pair<CFANode, String>> obtainInterruptPoints(final ThreadingIntpState threadingState, CFAEdge cfaedge) {
        Set<Pair<CFANode, String>> canIntpPoints = new HashSet<>();
        CFANode sucNode = cfaedge.getSuccessor(), preNode = cfaedge.getPredecessor();
        Map<String, List<DelayStrategy>> delayStrategyEdgeR = threadingState.getDelayStrategyREdge();
        Map<String, List<DelayStrategy>> delayStrategyEdgeW = threadingState.getDelayStrategyWEdge();

        // 处理当前是 enable 的情况
        String callFuncName = getFunctionCallName(cfaedge);
        if (callFuncName != null && callFuncName.startsWith(enIntpFunc) && !(delayStrategyEdgeW.isEmpty() && delayStrategyEdgeR.isEmpty())) {
            canIntpPoints.addAll(from(repPoints.get(sucNode)).transform(f -> Pair.of(sucNode, f)).toSet());
        }

        for (int i = 0; i < sucNode.getNumLeavingEdges(); ++i) {

            CFAEdge sucedge = sucNode.getLeavingEdge(i);
            // 末位触发
            if (sucedge instanceof CReturnStatementEdge) {
                String bottomTriggered = threadingState.getBottomTriggered();
                if (bottomTriggered != null) {
                    Set<String> intpFuncSet = getcanIntpFunc(threadingState);
                    for (String intpFunc : intpFuncSet) {
                        Map<String, Set<String>> intpRWSharedVarSet = intpFuncRWSharedVarMap.get(intpFunc);   //  intpFunc is global variables set in current isr
                        if (intpRWSharedVarSet.containsKey(bottomTriggered) && intpRWSharedVarSet.get(bottomTriggered).contains("R")) {
                            canIntpPoints.add(Pair.of(sucNode,intpFunc));
                        }
                    }
                }
            }

            if (repPoints.containsKey(sucNode)) {
                EdgeVtx sucedgeInfo = (EdgeVtx) condDepGraph.getDGNode(sucedge.hashCode());
                callFuncName = getFunctionCallName(sucedge);

                // 延迟策略 —— 如果是disbale
                if (callFuncName != null && callFuncName.startsWith(disIntpFunc) && repPoints.containsKey(sucNode)) {
                    Set<String> sucIntp = repPoints.get(sucNode);
                    if (delayStrategyEdgeR.isEmpty()) {
                        for (String var : delayStrategyEdgeR.keySet()) {
                            List<DelayStrategy> delayStrategiesList = delayStrategyEdgeR.get(var);
                            for (DelayStrategy delayStrategy : delayStrategiesList) {
                                Set<String> lastNodeIntp = delayStrategy.getIntpFunc();
                                if (!Sets.intersection(lastNodeIntp, sucIntp).isEmpty()) {
                                    canIntpPoints.addAll(from(repPoints.get(sucNode)).transform(f -> Pair.of(sucNode, f)).toSet());
                                    delayStrategy.drop(sucIntp);
                                }
                            }


                        }
                    }
                    if (delayStrategyEdgeW.isEmpty()) {
                        for (String var : delayStrategyEdgeW.keySet()) {
                            List<DelayStrategy> delayStrategiesList = delayStrategyEdgeW.get(var);
                            for (DelayStrategy delayStrategy : delayStrategiesList) {
                                CFANode lastNode = delayStrategy.getCfaEdge().getPredecessor();
                                if (!Sets.intersection(repPoints.get(lastNode), sucIntp).isEmpty()) {
                                    canIntpPoints.addAll(from(repPoints.get(sucNode)).transform(f -> Pair.of(sucNode, f)).toSet());
                                    delayStrategy.drop(sucIntp);
                                }
                            }
                        }
                    }
                    continue;
                }

                if (sucedgeInfo != null) { // the edge has global variables

                    // read or write of the subsequent edge
                    Set<String> edgeRWSharedVarSet = new HashSet<>();
                    edgeRWSharedVarSet.addAll(from(sucedgeInfo.getgReadVars()).transform(v -> v.getName()).toSet());
                    edgeRWSharedVarSet.addAll(from(sucedgeInfo.getgWriteVars()).transform(v -> v.getName()).toSet());

                    // successor can be interrupted isr and traverse it
                    Set<String> intpFuncSet = repPoints.get(sucNode);
                    for (String intpFunc : intpFuncSet) {
                        Map<String, Set<String>> intpRWSharedVarSet = intpFuncRWSharedVarMap.get(intpFunc);   //  intpFunc is global variables set in current isr
                        for (String var : edgeRWSharedVarSet) {     // var in subsequent edge
                            if (intpRWSharedVarSet.containsKey(var) && intpRWSharedVarSet.get(var).contains("W")) {   // if var in isr and write it
                                for (String var1 : delayStrategyEdgeR.keySet()) {  // Iterate over the previous read and write information that occurred
                                    if (intpRWSharedVarSet.containsKey(var1)) {
                                        for (DelayStrategy delayStrategy : delayStrategyEdgeR.get(var1)) {
                                            if (delayStrategy.getIntpFunc().contains(intpFunc)) {    // if the delayed interrupt is not inserted
                                                canIntpPoints.addAll(from(repPoints.get(sucNode)).transform(f -> Pair.of(sucNode, f)).toSet());
                                                delayStrategy.removeIntpFunc(intpFunc);
                                            }
                                        }

                                    }
                                }

                            }
                            if (!delayStrategyEdgeW.isEmpty() && intpRWSharedVarSet.containsKey(var)) {
                                for (String var1 : delayStrategyEdgeW.keySet()) {    // 之前有写就插
                                    if (intpRWSharedVarSet.containsKey(var1)) {
                                        canIntpPoints.addAll(from(repPoints.get(sucNode)).transform(f -> Pair.of(sucNode, f)).toSet());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return canIntpPoints;
    }

    public static String removeCloneInfoOfFuncName(String funcName) {
        if (funcName != null) {
            return funcName.contains(CFACloner.SEPARATOR) ? funcName.substring(0, funcName.indexOf(CFACloner.SEPARATOR)) : funcName;
        }
        return funcName;
    }

    public int getLowestPriority() {
        return intpPriOrder.equals(InterruptPriorityOrder.BH) ? ThreadingIntpState.MIN_PRIORITY_NUMBER : ThreadingIntpState.MAX_PRIORITY_NUMBER;
    }

}
