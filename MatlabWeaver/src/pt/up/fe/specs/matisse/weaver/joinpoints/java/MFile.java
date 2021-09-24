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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.NestedFunctionSt;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabProcessor.Utils.VariableTable;

import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFile;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunction;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ASection;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.AFunctionFtypeEnum;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.utils.SelectionUtils;
import pt.up.fe.specs.matisse.weaver.options.MWeaverKeys;
import pt.up.fe.specs.matisse.weaver.utils.VarType;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.FunctionAdapter;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.FunctionNodeAdapter;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.NestedFunctionAdapter;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;

/**
 * @author Tiago Carvalho Join point file using the {@link MatlabNode} approach
 */

public class MFile extends AFile {

    private final String name;
    // private final String absolutePath;

    private VarType typeDef;
    private final File sourceFile;
    private FileNode matlabRoot;
    private final VariableTable varTable;

    // TODO: Replace with DataStore
    // private final SetupBuilder aspectData;
    private final ScopedMap<String> variableDefinitions;

    public MFile(File source, AMWeaverJoinPoint parent) {
        this(source.getAbsolutePath(), SpecsIo.read(source), parent);
    }

    public MFile(String filename, String code, AMWeaverJoinPoint parent) {
        // Parent must be an App
        assert parent instanceof MApp;

        initMWeaverJP(parent);

        // sourceFile = source;
        sourceFile = new File(filename);

        // LanguageMode languageMode = getAspectData().get(MWeaverKeys.LANGUAGE);
        LanguageMode languageMode = MWeaverUtils.getWeaver().getSettings().get(MWeaverKeys.LANGUAGE);
        // FileNode matlabIR = new MatlabParser(languageMode).parse(sourceFile);
        FileNode matlabIR = new MatlabParser(languageMode).parse(code);
        setMatlabRoot(matlabIR);

        // Super fields
        name = sourceFile.getName();
        // absolutePath = sourceFile.getAbsolutePath();

        varTable = new VariableTable();
        varTable.addToken(name, matlabIR);

        // TODO: Move to App
        // aspectData = MatlabToCOptionUtils.newDefaultSetup();
        // helper = aspectData.getA();
        variableDefinitions = new ScopedMap<>();
    }

    @Override
    public MApp getParentImpl() {
        return (MApp) super.getParentImpl();
    }

    /**
     * @return the aspectData
     */
    // public DataStore getAspectData() {
    // // return aspectData;
    // return getParent().getAspectData();
    // }

    public ScopedMap<String> getVariableDefinitions() {
        return variableDefinitions;
    }

    /**
     * Select all functions inside the sourceFile
     */
    @Override
    public List<? extends AFunction> selectFunction() {
        // Function nodes
        List<FunctionNode> functions = matlabRoot.getUnits().stream()
                .filter(unit -> unit instanceof FunctionNode)
                .map(unit -> (FunctionNode) unit)
                .collect(Collectors.toList());

        if (functions.isEmpty()) {
            return Collections.emptyList();
        }

        List<AFunction> joinpoints = new ArrayList<>();

        functionListAddFunctionOrSubfunction(joinpoints, functions.get(0), AFunctionFtypeEnum.MAIN_FUNCTION);
        for (int i = 1; i < functions.size(); i++) {
            functionListAddFunctionOrSubfunction(joinpoints, functions.get(i), AFunctionFtypeEnum.SUB_FUNCTION);
        }

        return joinpoints;
    }

    /**
     * Add new {@link MFunction} to the functions list
     * 
     * @param functions
     *            the list of functions
     * @param function
     *            the function to add
     * @param isMainFunction
     *            is the new function the file main function?
     */
    private void functionListAddFunctionOrSubfunction(List<AFunction> functions, FunctionNode function,
            AFunctionFtypeEnum functionType) {

        MFunction mFunction = new MFunction(new FunctionNodeAdapter(function), this, functionType);
        functions.add(mFunction);

        // Check if there are nested functions inside the function
        // Nested functions can only appear outside Blocks (e.g., if, for, while...), so searching just on the children
        // of functions is ok

        List<NestedFunctionSt> nestedFunctions = function.getChildren(NestedFunctionSt.class);

        // Create joinpoints for nested functions
        List<MFunction> nestedFunctionJps = createNestedFunctionJps(this, mFunction, nestedFunctions);

        // Add nested functions
        functions.addAll(nestedFunctionJps);
    }

    private static List<MFunction> createNestedFunctionJps(MFile mFile, MFunction parent,
            List<NestedFunctionSt> nestedFunctions) {

        List<MFunction> functionJps = new ArrayList<>();

        for (NestedFunctionSt nestedFunction : nestedFunctions) {
            // Create nested function joinpoint
            FunctionAdapter fAdapter = new NestedFunctionAdapter(nestedFunction);
            MFunction nestedFunctionJp = new MFunction(fAdapter, parent, AFunctionFtypeEnum.NESTED_FUNCTION);

            functionJps.add(nestedFunctionJp);

            List<NestedFunctionSt> nestedChildren = nestedFunction.getChildren(NestedFunctionSt.class);

            // Create a joinpoint for each nested function
            functionJps.addAll(createNestedFunctionJps(mFile, nestedFunctionJp, nestedChildren));
        }

        return functionJps;
    }

    @Override
    public List<? extends ASection> selectSection() {
        // Find sections in the file units
        List<CommentSt> sections = matlabRoot.getUnits().stream()
                .map(function -> SelectionUtils.getSectionNodes(function.getStatements()))
                .reduce(new ArrayList<>(), SpecsCollections::add);

        if (sections.isEmpty()) {
            return Collections.emptyList();
        }

        List<ASection> joinpoints = new ArrayList<>();

        sections.forEach(section -> joinpoints.add(new MSection(section, this)));

        return joinpoints;
    }

    @Override
    public List<? extends AComment> selectComment() {

        return matlabRoot.getDescendantsStream()
                .filter(CommentSt.class::isInstance)
                .map(comment -> new MComment((CommentSt) comment, this))
                .collect(Collectors.toList());
    }

    public boolean containsVariable(String functionName, String varName) {
        return varTable.containsVariable(name, functionName, varName);
    }

    /**
     * @return the matlabRoot
     */
    public MatlabNode getMatlabRoot() {
        return matlabRoot;
    }

    /**
     * @param matlabRoot
     *            the matlabRoot to set
     */
    public void setMatlabRoot(FileNode matlabRoot) {
        this.matlabRoot = matlabRoot;
    }

    public File getSourceFile() {
        return new File(matlabRoot.getFilename());
    }

    /**
     * @return the typeDef
     */
    public VarType getTypeDef() {
        return typeDef;
    }

    /**
     * @param typeDef
     *            the typeDef to set
     */
    /*
    public void setTypeDef(VarType typeDef) {
    this.typeDef = typeDef;
    }
    */
    /*
    public void put(String function, String var, String type) {
    TypesParser typesParser = new TypesParser(getAspectData());
    TypesMap typesMap = getAspectData().getA().value(MatisseOption.TYPE_DEFINITION, TypesMap.class);
    
    VariableType parsedType = typesParser.parse(type);
    if (parsedType == null) {
    
        LoggingUtils.msgInfo("Ignoring type definition '" + type + "' for variable '" + var
    	    + "' in function " + function);
    
        return;
    }
    
    typesMap.addSymbol(key, symbol);
    
    // return typeDef.put(function, var, type);
    }
    */

    @Override
    public String toString() {
        return "MFILE(name: " + name + ")";
    }

    @Override
    public MatlabNode getNode() {
        return matlabRoot;
    }

    @Override
    public String getNameImpl() {
        return name;
    }

    @Override
    public String getAbsolutePathImpl() {
        try {
            return sourceFile.getAbsolutePath();
        } catch (Exception e) {
            SpecsLogs.msgInfo("No absolute path available, returning filename");
            return sourceFile.getName();
        }

        // return absolutePath;
    }

    @Override
    public String getMainFunctionImpl() {
        if (!(matlabRoot instanceof FunctionFileNode)) {
            return "";
        }

        return ((FunctionFileNode) matlabRoot).getMainFunctionName();
    }

    @Override
    public AJoinPoint getMainFunctionJpImpl() {
        return selectFunction().stream().findFirst().orElse(null);
    }

    @Override
    public void detachImpl() {
        // Remove from root
        getParentImpl().removeFile(sourceFile);
    }

}
