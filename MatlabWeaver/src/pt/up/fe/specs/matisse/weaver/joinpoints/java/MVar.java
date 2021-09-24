/*
 * Copyright 2013 SPeCS.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.VariableTable;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ForSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.LoopSt;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.AVarReferenceEnum;
import pt.up.fe.specs.matisse.weaver.entities.Sym;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.enums.VarDefines;
import pt.up.fe.specs.matisse.weaver.joinpoints.enums.VariableType;
import pt.up.fe.specs.matisse.weaver.utils.Action;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

public class MVar extends AVar {

    public final static List<String> SCRIPT_KEY = Arrays.asList("_script");

    // private static final OptionHelper SUPPORTED_OPTIONS;
    private static final Map<String, DataKey<?>> SUPPORTED_OPTIONS;

    static {
        // SUPPORTED_OPTIONS = new OptionHelper();
        SUPPORTED_OPTIONS = new HashMap<>();

        // MVar.SUPPORTED_OPTIONS.add("constant", MatisseOption.CONSTANT_VARIABLES);
        MVar.SUPPORTED_OPTIONS.put("constant", MatisseKeys.CONSTANT_VARIABLES);
    }

    private static DataKey<?> getDefinition(String optionName, boolean verbose) {
        boolean isCaseInsensitive = false;

        if (isCaseInsensitive) {
            optionName = optionName.toLowerCase();
        }

        DataKey<?> def = MVar.SUPPORTED_OPTIONS.get(optionName);

        if (def == null) {
            if (verbose) {
                SpecsLogs.msgInfo("[!] Option '" + optionName
                        + "' is not defined. Available options:");

                // Get keys
                List<String> keys = SpecsFactory.newArrayList(MVar.SUPPORTED_OPTIONS.keySet());
                // Sort them alphabetically
                Collections.sort(keys);
                // Show keys
                for (String key : keys) {
                    SpecsLogs.msgInfo("      - " + key);
                }
            }
            return null;
        }

        return def;
    }

    private final IdentifierNode idNode;
    private final AVarReferenceEnum enumReference;

    // private final VariableType type;

    /*
    public MVar(IdentifierNode var, AJoinPoint parent, boolean inout) {
    this(var, parent, inout ? VariableType.INPUT : VariableType.LOCAL);
    }
     */
    public MVar(IdentifierNode var, AMWeaverJoinPoint parent, VariableType type) {
        this(var, parent, type, getReadWrite(var, type));
    }

    public MVar(IdentifierNode var, AMWeaverJoinPoint parent, VariableType type, AVarReferenceEnum reference) {
        super(new MExpression(var, parent));

        initMWeaverJP(parent);

        idNode = var;
        // this.type = type;

        // this.name = idNode.getName();
        // this.reference = getReadWrite(idNode, type);
        enumReference = reference;
        // this.is_read = reference == ReferenceEnum.READ;
        // this.is_write = reference == ReferenceEnum.WRITE;

        // TODO: inout is not enough, we want to know if it is an input or an output argument
    }

    @Override
    public String getNameImpl() {
        return idNode.getName();
    }

    @Override
    public Sym getSymImpl() {
        throw new RuntimeException("NOT IMPLEMENTED!");
    }

    @Override
    public Boolean getIs_readImpl() {
        return enumReference == AVarReferenceEnum.READ;
    }

    @Override
    public Boolean getIs_writeImpl() {
        return enumReference == AVarReferenceEnum.WRITE;
    }

    @Override
    public String getReferenceImpl() {
        return enumReference.toString();
    }

    private static AVarReferenceEnum getReadWrite(IdentifierNode idNode, VariableType type) {

        if (type == VariableType.INPUT || type == VariableType.OUTPUT) {
            return AVarReferenceEnum.ARGUMENT;
        }

        // If on the left side of an argument, is write
        Optional<AssignmentSt> assignSt = idNode.getAncestorTry(AssignmentSt.class);
        if (assignSt.isPresent()) {
            // Check if on the left side
            Optional<MatlabNode> self = assignSt.get().getLeftHand().getDescendantsAndSelfStream()
                    .filter(node -> node == idNode)
                    .findFirst();

            if (self.isPresent()) {
                return AVarReferenceEnum.WRITE;
            }
        }

        // If the first child of a For statement, is write
        if (idNode.getParent() instanceof ForSt) {
            ForSt forSt = (ForSt) idNode.getParent();
            if (forSt.getIndexVar() == idNode) {
                return AVarReferenceEnum.WRITE;
            }
        }

        // Is there any more cases where a variable can be written?
        return AVarReferenceEnum.READ;
    }

    @Override
    public void defImpl(String attribute, Object value) {

        // LARA attributes

        // Check if attribute is supported
        DataKey<?> def = getDefinition(attribute, false);

        if (def != null) {
            // Get option table
            // TODO: Adapt to App, now MFile does not contain the aspectData
            // MFile mFile = MJoinpointUtils.getAncestor(this, MFile.class).get();
            // DataStore aspectData = mFile.getAspectData();
            DataStore aspectData = MWeaverUtils.getWeaver().getSettings();

            // Check if CONSTANT_VARIABLES
            if (MatisseKeys.CONSTANT_VARIABLES.getName().equals(def.getName())) {

                // if (OptionUtils.areEqual(MatisseOption.CONSTANT_VARIABLES, def)) {
                // Option option = aspectData.getOption(def);
                // VariableTable table = (VariableTable) option.getValue();
                defConstantVar(aspectData.get(MatisseKeys.CONSTANT_VARIABLES), value);
                return;
            }

            // For remaining attributes, treat the value as a raw String
            String stringValue = value.toString();

            // aspectData.setOption(OptionUtils.newOption(def, stringValue));
            aspectData.setRaw(def, def.getDecoder().get().decode(stringValue));

            return;

        }

        // TODO: Adapt to App, now MFile does not contain the aspectData
        applyAttributes(attribute, value);
    }

    /**
     * @param attribute
     * @param value
     */
    private void applyAttributes(String attribute, Object value) {

        // Get attributes
        String ATTRIBUTE = attribute.toUpperCase();
        if (!VarDefines.contains(ATTRIBUTE)) {
            System.err.println("On action def, in variable " + getName() + ": attribute " + attribute
                    + " cannot be defined!");
            return;
        }

        VarDefines varDefine = VarDefines.valueOf(ATTRIBUTE);

        // MFile mFile = (MFile) getFileRoot();
        switch (varDefine) {
        case TYPE:
            // Get function joinpoint
            MFunction funcParent = MJoinpointUtils.getAncestor(this, MFunction.class).orElseThrow(
                    () -> new RuntimeException("Could not find function parent for variable " + getName()));

            MApp mApp = MJoinpointUtils.getAncestor(funcParent, MApp.class).orElseThrow(
                    () -> new RuntimeException("Could not find file parent for function " + funcParent.getName()));

            // For old method of storing value types (to be written to a types file)
            // mFile.put(funcParent.getName(), getName(), value.toString());

            // System.out.println("OLD METHOD:\n" + mFile.getTypeDef());
            // New method of storing value types (directly into the Setup)
            List<String> scope = Arrays.asList(funcParent.getQualifiedNameArrayImpl());
            String varName = getNameImpl();
            String typeString = value.toString();

            // mFile.getVariableDefinitions().addSymbol(scope, varName, typeString);
            mApp.addVariableDef(scope, varName, typeString);
            // System.out.println("NEW METHOD:\n" + mFile.getVariableDefinitions());
            // addVarType(mFile, function.getQualifiedName(), this.name, value.toString());
            break;
        case DEFAULT:
            // TODO: Implement this action directly in LARA
            Action.replaceInput(idNode, value.toString());
            // Replace an input with a default value

            // FileNode assignMatlabValue = new MatlabParser().parse(value.toString());
            // TomToken assignTokenValue = TomTokenUtils.generateTomToken(assignMatlabValue);

            // TomToken newRoot = TomActions.defDefault(mFile.getTomRoot(), this.idNode, assignTokenValue);
            // mFile.setTomRoot(newRoot);

            break;
        default:
            throw new RuntimeException("Action def for attribute " + attribute + " not implemented");
        }
    }

    /**
     * @param aspectData
     * @param qualifiedName
     * @param name
     * @param string
     */
    /*
    private void addVarType(Setup setup, List<String> scope, String varName, String varTypeString) {
    //private void addVarType(MFile mFile, List<String> scope, String varName, String varTypeString) {
    // Store information about variable type
    //mFile.getVariableDefinitions().addSymbol(scope, varName, varTypeString);
    SetupHelper helper = new SetupHelper(setup);
    
    // Parse variable type
    VariableType varType = new TypesParser(setup).parse(varTypeString);
    if (varType == null) {
        LoggingUtils.msgInfo("Ignoring type definition for variable '" + varName + "' in function " + scope);
        return;
    }
    
    // Get types map
    TypesMap typesMap = helper.getValue(MatisseOption.TYPE_DEFINITION, TypesMap.class);
    
    // Add type to table
    typesMap.addSymbol(scope, varName, varType);
    }
     */

    /**
     * @param table
     * @param value
     */
    private void defConstantVar(VariableTable table, Object value) {
        // Parse value as a boolean
        boolean addVar = Boolean.parseBoolean(value.toString());

        // Get function name
        List<String> functionName = MJoinpointUtils.getFunctionName(this);

        // If no function name found, belongs to a script
        if (functionName == null) {
            functionName = MVar.SCRIPT_KEY;
            // functionName = VariableTable.SCRIPT_KEY;
            // throw new RuntimeException("Could not find function parent for variable " + name);
        }

        // Add variable
        if (addVar) {
            table.addVariable(functionName, getNameImpl());
        }
        // Remove variable
        else {
            table.removeVariable(functionName, getNameImpl());
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "MVar(name: " + getName() + ")";
    }

    @Override
    public MatlabNode getNode() {
        return idNode;
    }

    @Override
    public Boolean getIsReadImpl() {
        return enumReference == AVarReferenceEnum.READ;
    }

    @Override
    public Boolean getIsWriteImpl() {
        return enumReference == AVarReferenceEnum.WRITE;
    }

    @Override
    public Boolean getIsInsideLoopHeaderImpl() {
        // Go back in the AST until it finds a statement
        StatementNode firstStmtAncestor = getNode().getAncestor(StatementNode.class);

        // If the statement is a loop statement, it is inside a loop header
        return firstStmtAncestor instanceof LoopSt;
    }

}
