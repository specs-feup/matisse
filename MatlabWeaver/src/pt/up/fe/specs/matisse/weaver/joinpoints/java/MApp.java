/**
 * Copyright 2013 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.specs.CIR.CirKeys;
import org.specs.CIR.CirUtils;
import org.specs.CIR.Types.FunctionName;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.Inlining.InliningData;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabAspects.MatlabAspectsUtils;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matlabtocl.v2.CLSetupUtils;
import org.specs.matlabtocl.v2.MatisseCLKeys;
import org.specs.matlabtocl.v2.codegen.MatrixCopyToGpuStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumLocalReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumReductionStrategy;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumSubgroupReductionStrategy;
import org.specs.matlabtocl.v2.heuristics.decisiontree.DecisionNode;
import org.specs.matlabtocl.v2.heuristics.decisiontree.Node;
import org.specs.matlabtocl.v2.heuristics.decisiontree.TerminalNode;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleDecisionTree;
import org.specs.matlabtocl.v2.heuristics.schedule.ScheduleMethod;
import org.specs.matlabtocl.v2.heuristics.schedule.SchedulePredictorContext;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.specs.matlabtocl.v2.ssa.ScheduleStrategy;
import org.specs.matlabtocl.v2.ssa.passes.GpuSVMEliminationMode;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.matisse.weaver.MWeaver;
import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AApp;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFile;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunction;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.utils.VarType;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import tdrc.utils.StringUtils;

/**
 * @author Tiago Join point root for the application using the {@link MatlabNode} approach
 * 
 */
public class MApp extends AApp {

    private static final Map<String, BiConsumer<DataStore, Object>> DEFS;
    static {
        DEFS = new HashMap<>();
        MApp.DEFS.put("inline", MApp::defInline);
        MApp.DEFS.put("default_float", MApp::defDefaultFloat);
        MApp.DEFS.put("use_blas", MApp::defUseBlas);
        MApp.DEFS.put("use_z3", MApp::defUseZ3);
        MApp.DEFS.put("allow_dynamic_allocation", MApp::defAllowDynamicAllocation);

        MApp.DEFS.put("sub_group_size", MApp::defSubGroupSize);
        MApp.DEFS.put("sum_reduction_strategy", MApp::defSumReductionStrategy);
        MApp.DEFS.put("matrix_copy_strategy", MApp::defMatrixCopyStrategy);
        MApp.DEFS.put("default_coarsening_strategy", MApp::defDefaultCoarseningStrategy);
        MApp.DEFS.put("default_fixed_work_groups_strategy", MApp::defDefaultFixedWorkGroupsStrategy);
        MApp.DEFS.put("max_work_item_dimensions", MApp::defMaxWorkItemDimensions);
        MApp.DEFS.put("device_memory_management_strategy", MApp::defDeviceMemoryManagementStrategy);
        MApp.DEFS.put("svm_elimination_mode", MApp::defSVMEliminationMode);
        MApp.DEFS.put("range_set_instruction_enabled", MApp::defRangeSetInstructionEnabled);
        MApp.DEFS.put("svm_restrict_coalesced", MApp::defSvmRestrictCoalesced);
        MApp.DEFS.put("svm_restrict_sequential", MApp::defSvmRestrictSequential);
        MApp.DEFS.put("svm_set_range_forbidden", MApp::defSvmSetRangeForbidden);
        MApp.DEFS.put("program_compiled_name", MApp::defProgramCompiledName);
        MApp.DEFS.put("load_program_from_source", MApp::defLoadProgramFromSource);
        MApp.DEFS.put("try_use_schedule_cooperative", MApp::defTryUseScheduleCooperative);
        MApp.DEFS.put("prefer_subgroup_cooperative", MApp::defPreferSubGroupCooperative);
        MApp.DEFS.put("sum_sub_group_reduction_strategy", MApp::defSumSubGroupReductionStrategy);
        MApp.DEFS.put("sum_local_reduction_strategy", MApp::defSumLocalReductionStrategy);
        MApp.DEFS.put("sub_group_as_warp_fallback", MApp::defSubGroupAsWarpFallback);

        MApp.DEFS.put("schedule_decision_tree", MApp::defScheduleDecisionTree);
    }

    private final Map<File, AFile> currentFiles;

    // private final List<AFile> files;
    // private final Set<File> currentFiles;

    // private final DataStore aspectData;

    public MApp(List<File> source, DataStore setup) {
        initMWeaverJP(null);

        // files = new ArrayList<>(source.size());
        // currentFiles = new HashSet<>(source);
        currentFiles = new LinkedHashMap<>(source.size());

        // aspectData = setup;
        // source.forEach(file -> files.add(new MFile(file, this)));
        source.forEach(file -> currentFiles.put(file, new MFile(file, this)));
    }

    // public DataStore getAspectData() {
    // return aspectData;
    // }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AProgramRoot#selectFile()
     */
    @Override
    public List<? extends AFile> selectFile() {
        // return files;
        return new ArrayList<>(currentFiles.values());
    }

    @Override
    public Long getUidImpl() {
        // return (long) files.hashCode();
        return (long) currentFiles.hashCode();
    }

    @Override
    public MatlabNode getNode() {
        throw new UnsupportedOperationException("There is no AST node for joinpoint App");
    }

    @Override
    public String getAstImpl() {
        // return StringUtils.join(files, AFile::getAstImpl, "\n\n");
        return StringUtils.join(currentFiles.values(), AFile::getAstImpl, "\n\n");
    }

    @Override
    public String getXmlImpl() {
        // return StringUtils.join(files, AFile::getXmlImpl, "\n\n");
        return StringUtils.join(currentFiles.values(), AFile::getXmlImpl, "\n\n");
    }

    /**
     * Needs to override since there is no AST node for joinpoint App
     */
    @Override
    public boolean compareNodes(AJoinPoint aJoinPoint) {
        return equals(aJoinPoint);
    }

    private static void defUseBlas(DataStore setup, Object value) {
        var optimizations = setup.get(MatlabToCKeys.MATISSE_OPTIMIZATIONS);

        // Decode value
        Boolean boolValue = decodeBoolean("use_blas", value);
        if (boolValue) {
            optimizations.add(MatisseOptimization.UseBlas.getName());
        } else {
            optimizations.remove(MatisseOptimization.UseBlas.getName());
        }
    }

    private static Boolean decodeBoolean(String property, Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return Boolean.parseBoolean(value.toString());

        // throw new IllegalArgumentException("Property '" + property + "' expects boolean value, got '" + value + "'");

    }

    private static void defUseZ3(DataStore setup, Object value) {
        // Decode value
        Boolean boolValue = decodeBoolean("use_z3", value);

        setup.set(MatlabToCKeys.ENABLE_Z3, boolValue);
    }

    private static void defAllowDynamicAllocation(DataStore setup, Object value) {
        // Decode value
        Boolean boolValue = decodeBoolean("allow_dynamic_allocation", value);

        setup.set(CirKeys.ALLOW_DYNAMIC_ALLOCATION, boolValue);
    }

    private static void defDefaultFloat(DataStore setup, Object value) {
        VariableType type = new MatlabAspectsUtils(setup).getDecoder().decode(value.toString());

        if (type == null) {
            type = CirUtils.getDefaultRealType();
        }

        setup.set(CirKeys.DEFAULT_REAL, type);
    }

    private static void defInline(DataStore setup, Object value) {
        String function = value.toString();
        boolean set = true;
        if (function.startsWith("!")) {
            set = false;
            function = function.substring(1);
        }

        setInline(setup, function, set);
    }

    private static void defSubGroupSize(DataStore setup, Object value) {
        setup.set(MatisseCLKeys.SUB_GROUP_SIZE, Integer.valueOf(value.toString()));
    }

    private static void defSumReductionStrategy(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.SUM_REDUCTION_STRATEGY, SumReductionStrategy.valueOf(valueStr));
    }

    private static void defMatrixCopyStrategy(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.MATRIX_COPY_TO_GPU_STRATEGY, MatrixCopyToGpuStrategy.valueOf(valueStr));
    }

    private static void defDefaultCoarseningStrategy(DataStore setup, Object value) {
        String valueStr = value.toString();

        ScheduleStrategy distributionStrategy = ScheduleStrategy.valueOf(valueStr);

        if (distributionStrategy != ScheduleStrategy.COARSE_SEQUENTIAL &&
                distributionStrategy != ScheduleStrategy.COARSE_GLOBAL_ROTATION) {
            throw new IllegalArgumentException("Invalid coarsening strategy: " + distributionStrategy);
        }

        setup.set(MatisseCLKeys.COARSE_DISTRIBUTION_STRATEGY, distributionStrategy);
    }

    private static void defDefaultFixedWorkGroupsStrategy(DataStore setup, Object value) {
        String valueStr = value.toString();

        ScheduleStrategy distributionStrategy = ScheduleStrategy.valueOf(valueStr);

        if (distributionStrategy != ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL &&
                distributionStrategy != ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION) {
            throw new IllegalArgumentException("Invalid fixed work groups strategy: " + distributionStrategy);
        }

        setup.set(MatisseCLKeys.FIXED_WORK_GROUPS_DISTRIBUTION_STRATEGY, distributionStrategy);
    }

    private static void defMaxWorkItemDimensions(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.MAX_WORK_ITEM_DIMENSIONS, Integer.valueOf(valueStr));
    }

    private static void defDeviceMemoryManagementStrategy(DataStore setup, Object value) {
        String valueStr = value.toString();

        DeviceMemoryManagementStrategy strategy = DeviceMemoryManagementStrategy.valueOf(valueStr);
        CLSetupUtils.setDeviceManagementStrategy(setup, strategy);
    }

    private static void defSVMEliminationMode(DataStore setup, Object value) {
        String valueStr = value.toString();

        GpuSVMEliminationMode mode = GpuSVMEliminationMode.valueOf(valueStr);
        setup.set(MatisseCLKeys.SVM_ELIMINATION_MODE, mode);
    }

    private static void defRangeSetInstructionEnabled(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.RANGE_SET_INSTRUCTION_ENABLED, Boolean.valueOf(valueStr));
    }

    private static void defSvmRestrictCoalesced(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.SVM_RESTRICT_COALESCED, Boolean.valueOf(valueStr));
    }

    private static void defSvmRestrictSequential(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.SVM_RESTRICT_SEQUENTIAL, Boolean.valueOf(valueStr));
    }

    private static void defSvmSetRangeForbidden(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.SVM_SET_RANGE_FORBIDDEN, Boolean.valueOf(valueStr));
    }

    private static void defProgramCompiledName(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.PROGRAM_COMPILED_NAME, valueStr);
    }

    private static void defLoadProgramFromSource(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.LOAD_PROGRAM_FROM_SOURCE, Boolean.valueOf(valueStr));
    }

    private static void defTryUseScheduleCooperative(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.TRY_USE_SCHEDULE_COOPERATIVE, Boolean.valueOf(valueStr));
    }

    private static void defPreferSubGroupCooperative(DataStore setup, Object value) {
        String valueStr = value.toString();

        setup.set(MatisseCLKeys.PREFER_SUBGROUP_COOPERATIVE, Boolean.valueOf(valueStr));
    }

    private static void defSumSubGroupReductionStrategy(DataStore setup, Object value) {
        setup.set(MatisseCLKeys.SUM_SUBGROUP_REDUCTION_STRATEGY,
                SumSubgroupReductionStrategy.valueOf(value.toString()));
    }

    private static void defSumLocalReductionStrategy(DataStore setup, Object value) {
        setup.set(MatisseCLKeys.SUM_LOCAL_REDUCTION_STRATEGY,
                SumLocalReductionStrategy.valueOf(value.toString()));
    }

    private static void defSubGroupAsWarpFallback(DataStore setup, Object value) {
        setup.set(MatisseCLKeys.SUBGROUP_AS_WARP_FALLBACK,
                Boolean.valueOf(value.toString()));
    }

    private static void defScheduleDecisionTree(DataStore setup, Object value) {
        setup.set(MatisseCLKeys.SCHEDULE_DECISION_TREE, new ScheduleDecisionTree(getDecisionTreeNode(value)));
    }

    private static Node<ScheduleMethod, SchedulePredictorContext> getDecisionTreeNode(Object obj) {
        var jsEngine = MWeaver.getThreadLocalWeaver().getScriptEngine();

        if (!jsEngine.isArray(obj)) {
            throw new IllegalArgumentException("Expected array, got " + obj.toString());
        }

        var values = new ArrayList<>(jsEngine.getValues(obj));

        if (values.isEmpty()) {
            throw new IllegalArgumentException("Got empty array");
        }
        String nodeType = values.get(0).toString();
        if (nodeType.equals("rule")) {
            if (values.size() < 5) {
                throw new IllegalArgumentException("Too few elements at rule " + obj.toString());
            }
            String ruleName = values.get(1).toString();

            Object minimumObj = values.get(2);
            if (!jsEngine.isNumber(minimumObj)) {
                throw new IllegalArgumentException("Expected minimum value (number), got " + minimumObj.toString());
            }

            double minimum = jsEngine.asDouble(minimumObj);

            return new DecisionNode<>(ruleName, minimum,
                    getDecisionTreeNode(values.get(3)),
                    getDecisionTreeNode(values.get(4)));
        } else if (nodeType.equals("terminal")) {
            if (values.size() < 2) {
                throw new IllegalArgumentException("Too few elements in terminal " + obj.toString());
            }

            ScheduleStrategy schedule = ScheduleStrategy.valueOf(values.get(1).toString());
            if (schedule == ScheduleStrategy.AUTO) {
                throw new IllegalArgumentException("AUTO schedule is invalid in the decision tree.");
            }

            List<Integer> terminalValues = new ArrayList<>();
            for (int slot = 2; slot < values.size(); ++slot) {
                Object value = values.get(slot);

                if (!jsEngine.isNumber(value)) {
                    throw new IllegalArgumentException("Expected number, got " + value);
                }

                Number valueDouble = jsEngine.asDouble(value);
                ;
                if (valueDouble.doubleValue() == valueDouble.intValue()) {
                    terminalValues.add(valueDouble.intValue());
                } else {
                    throw new IllegalArgumentException("Expected integer");
                }
            }

            return new TerminalNode<>((c, r) -> new ScheduleMethod(schedule, terminalValues));
        } else if (nodeType.equals("terminal_function")) {
            if (values.size() < 2) {
                throw new IllegalArgumentException("Too few elements in terminal_function " + obj.toString());
            }

            String functionCode = values.get(1).toString();

            return new TerminalNode<ScheduleMethod, SchedulePredictorContext>((c, r) -> {
                var evalFunc = jsEngine.eval(functionCode);

                // evalFunc = (JSObject) new NashornScriptEngineFactory().getScriptEngine().eval(functionCode);
                // return getDecisionTreeNode(evalFunc.call(null, c)).decide(c, r);
                return getDecisionTreeNode(jsEngine.call(evalFunc, null, c)).decide(c, r);

            });
        } else {
            throw new NotImplementedException(nodeType);
        }

    }

    private static void setInline(DataStore setup, String value, boolean set) {
        InliningData data = setup.get(CirKeys.INLINE);

        // Determine Inlinable
        FunctionName id = data.getId(value);

        if (id == null) {
            SpecsLogs.msgInfo("Value '" + value + "' not supported for option 'inline'.");
            SpecsLogs.msgInfo(" Supported values:");
            for (FunctionName name : data.getSupportedFunctions()) {
                SpecsLogs.msgInfo(" - " + name);
            }
            return;
        }

        if (set) {
            data.setInline(id);
        } else {
            data.unsetInline(id);
        }
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AJoinPoint#def(java.lang.String, java.lang.Object)
     */
    @Override
    public void defImpl(String attribute, Object value) {

        MApp.DEFS.getOrDefault(attribute,
                (setup, a_value) -> SpecsLogs
                        .msgWarn("On action def of app, attribute '" + attribute + "' is not valid"))
                // .accept(getAspectData(), value);
                .accept(MWeaverUtils.getWeaver().getSettings(), value);
    }

    @Override
    public void setGlobalTypeImpl(String variable, String type) {
        addVariableDef(Arrays.asList("global"), variable, type);
    }

    public void addVariableDef(List<String> scope, String varName, String type) {
        // MatlabAspectsUtils typesParserV2 = new MatlabAspectsUtils(getAspectData());
        MatlabAspectsUtils typesParserV2 = new MatlabAspectsUtils(MWeaverUtils.getWeaver().getSettings());
        String typesContent = MatlabAspectsUtils.getTypesContent(scope, varName, type);
        assert typesContent != null;
        ScopedMap<VariableType> typesMapV2 = typesParserV2.getVariableTypes(typesContent);
        assert typesMapV2 != null;

        // TypesMap typesMap = getAspectData().get(MatisseKeys.TYPE_DEFINITION);
        TypesMap typesMap = MWeaverUtils.getWeaver().getSettings().get(MatisseKeys.TYPE_DEFINITION);

        typesMap.addSymbols(typesMapV2);
    }

    @Override
    public String getCodeImpl() {
        // return files.stream()
        return currentFiles.values().stream()
                .map(file -> "% File: " + file.getName() + "\n" + file.getCode())
                .collect(Collectors.joining("\n%%%%%%%%%%%%%%%\n\n"));

    }

    public boolean write(File outputFolder) {
        boolean writeStatus = true;

        for (AFile aFile : selectFile()) {
            StringBuilder outputFilePath = new StringBuilder(outputFolder.getAbsolutePath());
            outputFilePath.append(System.getProperty("file.separator"));

            // AFile aFile = root.selectFile().get(0);
            String fileName = aFile.getNameImpl();

            MFile mFile = (MFile) aFile;
            MatlabNode matlabToken = mFile.getMatlabRoot();
            VarType typeDef = mFile.getTypeDef();

            // Process options
            // aspectData = process(mFile);

            outputFilePath.append(fileName);
            File outputFile = new File(outputFilePath.toString());
            File typeFile = new File(outputFilePath.toString().replace(".m", "_typeDef.txt"));

            List<? extends AFunction> functionsJp = aFile.selectFunction();
            String mainFunction = null;
            if (!functionsJp.isEmpty()) {
                mainFunction = functionsJp.get(0).getNameImpl();
            }

            if (typeDef != null && !typeDef.isEmpty()) {

                String typeDefString = typeDef.toString(mainFunction);
                SpecsIo.write(typeFile, typeDefString);

                /*
                if (settings.get(MWeaverKeys.MATLABTYPES)) {
                    // fromTom = true;
                    MatlabAspects ma = new MatlabAspects();
                    @SuppressWarnings("deprecation")
                    SymbolTable st = ma.getSymbolTable(typeDefString);
                    if (st == null) {
                        return false;
                    }
                
                }
                */
            }

            if (matlabToken == null) {
                throw new RuntimeException("MatlabNode is null!");
            }

            String matlabCode = matlabToken.getCode();
            boolean lastWriteStatus = SpecsIo.write(outputFile, matlabCode);

            // Update until there is a false result
            if (writeStatus) {
                writeStatus = lastWriteStatus;
            }
        }

        return writeStatus;
    }

    @Override
    public AJoinPoint addFileImpl(String filename, String code) {
        // Check if file is already in the program
        File newFile = new File(filename);
        // if (currentFiles.contains(newFile)) {
        if (currentFiles.containsKey(newFile)) {
            SpecsLogs.msgInfo("App.addFile: replacing already existing file '" + filename + "' in the tree");
            // files.remove(newFile);
        }

        MFile mfile = new MFile(filename, code, this);
        // files.add(mfile);
        currentFiles.put(newFile, mfile);
        // currentFiles.add(newFile);
        return mfile;
    }

    @Override
    public Boolean hasFileImpl(String filename) {
        // return currentFiles.contains(new File(filename));
        return currentFiles.containsKey(new File(filename));
    }

    public AFile removeFile(String filename) {
        return removeFile(new File(filename));
    }

    public AFile removeFile(File file) {
        return currentFiles.remove(file);
    }
}
