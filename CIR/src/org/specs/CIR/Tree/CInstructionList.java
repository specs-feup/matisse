/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR.Tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.BlockNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CommentNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.PragmaNode;
import org.specs.CIR.Tree.CNodes.ReservedWordNode;
import org.specs.CIR.Tree.CNodes.ReturnNode;
import org.specs.CIR.Tree.CNodes.VerbatimNode;
import org.specs.CIR.Tree.CToCRules.CToCData;
import org.specs.CIR.Tree.CToCRules.CToCRules;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;

import com.google.common.collect.Lists;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.ClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * Represents a list of C instructions, in the form of CTokens.
 * 
 * @author Joao Bispo
 * 
 */
public class CInstructionList implements Iterable<CNode> {

    private final static ClassMap<CNode, InstructionType> TYPES_MAP;
    // private final static Map<CNodeType, InstructionType> typesMap;

    static {
        TYPES_MAP = new ClassMap<>();

        CInstructionList.TYPES_MAP.put(FunctionCallNode.class, InstructionType.FunctionCall);
        CInstructionList.TYPES_MAP.put(AssignmentNode.class, InstructionType.Assignment);
        CInstructionList.TYPES_MAP.put(BlockNode.class, InstructionType.Block);
        CInstructionList.TYPES_MAP.put(CommentNode.class, InstructionType.Comment);
        CInstructionList.TYPES_MAP.put(ReturnNode.class, InstructionType.Return);
        CInstructionList.TYPES_MAP.put(VerbatimNode.class, InstructionType.Literal);
        CInstructionList.TYPES_MAP.put(PragmaNode.class, InstructionType.Pragma);
    }

    private final BlockNode instructionsRoot;
    private final Map<String, Variable> literalVariables;
    private final Initializations variableInitialization;

    private FunctionType functionTypes;
    private boolean needsProcessing;

    // If true, CToCData will ignore some rules, such as resolution of temporary names
    private final boolean snippetMode;

    /**
     * Literal code might need additional instances.
     */
    // private Set<FunctionInstance> additionalInstances;

    /**
     * FunctionTypes can be initially null, but before getting the instructions it has to be set.
     */
    public CInstructionList(FunctionType functionTypes) {
        List<CNode> emptyList = Collections.emptyList();
        this.instructionsRoot = CNodeFactory.newBlock(emptyList);
        this.needsProcessing = true;
        this.functionTypes = functionTypes;
        this.literalVariables = SpecsFactory.newHashMap();
        this.variableInitialization = new Initializations();
        this.snippetMode = false;

    }

    public CInstructionList() {
        this(null);
    }

    /**
     * @param functionTypes
     *            the functionTypes to set
     */
    public void setFunctionTypes(FunctionType functionTypes) {
        if (this.functionTypes != null) {
            SpecsLogs.warn("Overwriting previous function types.");
        }

        this.functionTypes = functionTypes;
    }

    /**
     * @return the functionTypes
     */
    public FunctionType getFunctionTypes() {
        return this.functionTypes;
    }

    /**
     * Adds another CInstructionList.
     * 
     * <p>
     * This is the preferrable method to use, since it takes care of other things, such as adding initializations.
     * 
     * @param instructions
     */
    public void add(CInstructionList instructions) {
        addInstructions(instructions.get());
        getInitializations().add(instructions.getInitializations());
        this.literalVariables.putAll(instructions.literalVariables);
    }

    /**
     * Adds an instruction.
     * 
     * <p>
     * If the given token is not of type Instruction, tries to infer the instruction type. Warns the user if the
     * instruction could not be inferred.
     * 
     * @param ctoken
     */
    public void addInstruction(CNode ctoken) {
        if (ctoken instanceof InstructionNode) {
            addInstruction(ctoken, null);
            return;
        }

        // Infer type
        InstructionType type = CInstructionList.getInstructionType(ctoken);

        addInstruction(ctoken, type);
    }

    public void addInstruction(CNode ctoken, InstructionType type) {
        if (!(ctoken instanceof InstructionNode)) {
            if (type == null) {
                type = getInstructionType(ctoken.getClass());
            }

            ctoken = CNodeFactory.newInstruction(type, ctoken);
        }

        this.instructionsRoot.addChild(ctoken);

        // After new instruction is added, signal that it needs processing
        this.needsProcessing = true;

    }

    /**
     * Add several instructions.
     * 
     * @param tokens
     */
    public void addInstructions(List<CNode> tokens) {
        for (CNode token : tokens) {
            addInstruction(token);
        }
    }

    private static InstructionType getInstructionType(Class<? extends CNode> type) {
        // Try to find type from map
        InstructionType iType = CInstructionList.TYPES_MAP.get(type);

        if (iType != null) {
            return iType;
        }

        // Could not find type, throw exception
        SpecsLogs.warn("InstructionType not defined for CTokenType '" + type + "'.");
        iType = InstructionType.Undefined;

        return iType;
    }

    /**
     * Processes the instructions currently in the list, and returns them.
     * 
     * @return the instructions
     */
    // public List<InstructionNode> get() {
    public List<CNode> get() {
        if (this.needsProcessing) {
            processInstructions();
            this.needsProcessing = false;
        }

        return this.instructionsRoot.getChildren();
    }

    public CNode getRoot() {
        return this.instructionsRoot;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<CNode> iterator() {
        return get().iterator();
    }

    /**
     * @return
     */
    public String toStringTree() {
        StringBuilder builder = new StringBuilder();

        for (CNode instruction : this.instructionsRoot.getChildren()) {
            builder.append(instruction);
        }

        return builder.toString();
    }

    /**
     * Returns the C code for the corresponding functions.
     */
    @Override
    public String toString() {
        return this.instructionsRoot.getCode();
    }

    private void processInstructions() {

        // Apply CToC rules
        CToCData data = new CToCData(this.functionTypes, this.snippetMode);
        // Create a copy of the instructions to process, view may change during processing
        Collection<CNode> instructions = Lists.newArrayList(this.instructionsRoot.getChildren());
        // for (CToken instruction : instructionsRoot.getChildren()) {
        for (CNode instruction : instructions) {
            CToCRules.processCToken(instruction, data);
        }

    }

    /**
     * If the FunctionTypes have a return value, adds a return statement of the output variable to the end instruction
     * list. If not, adds an empty return.
     * 
     * @param cTypeData
     * @param instructionList
     */
    public void addReturn() {

        FunctionType cTypeData = getFunctionTypes();

        CNode returnValueToken = null;

        // Check if return type is different than void
        if (!VoidTypeUtils.isVoid(cTypeData.getCReturnType())) {
            String returnVarName = cTypeData.getCOutputName();
            VariableType returnVariable = cTypeData.getCReturnType();

            returnValueToken = CNodeFactory.newVariable(returnVarName, returnVariable);
        }

        addReturn(returnValueToken);
    }

    /**
     * Adds a return instruction to the end of the instruction list.
     * 
     * <p>
     * If given token in null, adds an empty return.
     * 
     * @param functionCall
     */
    public void addReturn(CNode returnValue) {

        CNode returnToken = null;
        if (returnValue == null) {
            returnToken = CNodeFactory.newReturn();
        } else {
            returnToken = CNodeFactory.newReturn(returnValue);
        }
        // CToken returnToken = CTokenFactory.newReturn(returnValue);

        addInstruction(returnToken, InstructionType.Return);
    }

    /**
     * Adds the given string as literal instructions. Each line in the string is considered as an instruction.
     * 
     * @param string
     */
    public void addLiteralInstruction(String string) {
        for (String literal : StringLines.newInstance(string)) {
            // Add instruction
            addInstruction(CNodeFactory.newLiteral(literal), InstructionType.Literal);
        }

    }

    public void addComment(String string) {
        addInstruction(CNodeFactory.newComment(string), InstructionType.Comment);
    }

    public void addAssignment(CNode assignment) {
        addInstruction(assignment, InstructionType.Assignment);
    }

    /**
     * Helper method that accepts an InstanceBuilder.
     * 
     * @param builder
     * @param arguments
     */
    public void addFunctionCall(InstanceBuilder builder, List<CNode> arguments) {
        addFunctionCall(builder.create(), arguments);
    }

    public void addFunctionCall(FunctionInstance function, CNode... arguments) {
        addFunctionCall(function, Arrays.asList(arguments));
    }

    public void addFunctionCall(FunctionInstance function, List<CNode> arguments) {
        CNode functionCall = function.newFunctionCall(arguments);
        addInstruction(functionCall, InstructionType.FunctionCall);
    }

    /**
     * @param numelsVariable
     * @param numelsCall
     */
    public void addAssignment(Variable leftHandVar, CNode rightHand) {
        addAssignment(CNodeFactory.newVariable(leftHandVar), rightHand);
    }

    /**
     * @param numelsVariable
     * @param numelsCall
     */
    public void addAssignment(CNode leftHandVar, CNode rightHand) {
        CNode assignmentToken = CNodeFactory.newAssignment(leftHandVar, rightHand);
        addAssignment(assignmentToken);
    }

    public void addLiteralVariable(Variable variable) {

        Variable previousVar = this.literalVariables.put(variable.getName(), variable);
        if (previousVar != null) {
            if (!previousVar.equals(variable)) {
                SpecsLogs.warn("Replacing var '" + previousVar + "' for var '" + variable + "'. Check if ok.");
            }
        }

    }

    public Initializations getInitializations() {
        return this.variableInitialization;
    }

    /**
     * @return the literalVariables
     */
    public Collection<Variable> getLiteralVariables() {
        return this.literalVariables.values();
    }

    /**
     * Builds an Instruction token, inferring the type of the instruction.
     * 
     * <p>
     * If the instruction type cannot be inferred, throws an exception.
     * 
     * @param token
     * @return
     */
    public static CNode newInstruction(CNode token) {
        // If already an instruction, return token
        if (token instanceof InstructionNode) {
            return token;
        }

        // Infer the instruction type
        InstructionType type = getInstructionType(token);

        // Build Instruction token
        return CNodeFactory.newInstruction(type, token);
    }

    /**
     * Tries to infer the instruction type.
     * 
     * <p>
     * If the instruction type cannot be inferred, throws an exception.
     * 
     * @param token
     * @return
     */
    public static InstructionType getInstructionType(CNode token) {

        if (token instanceof ReservedWordNode) {
            ReservedWord reservedWord = ((ReservedWordNode) token).getReservedWord();
            switch (reservedWord) {
            case Break:
                return InstructionType.Break;
            case Continue:
                return InstructionType.Continue;
            default:
                throw new NotImplementedException("Reserved word " + reservedWord);
            }
        }

        if (token instanceof InstructionNode) {
            return ((InstructionNode) token).getInstructionType();
        }

        return getInstructionType(token.getClass());
    }

    public void addIf(CNode condition, CNode... instructions) {
        addIf(condition, Arrays.asList(instructions));
    }

    /**
     * Adds an "if" block, with the given condition and instructions.
     * 
     * @param notEqualCall
     * @param ifInsts
     */
    public void addIf(CNode condition, List<CNode> instructions) {
        CNode ifBlock = IfNodes.newIfThen(condition, instructions);
        addInstruction(ifBlock);
    }

    /**
     * Adds an "while" block, with the given condition and instructions.
     */
    public void addWhile(CNode condition, List<CNode> instructions) {
        checkArgument(condition != null, "condition must not be null");
        checkArgument(instructions != null, "instructions must not be null");

        CNode whileToken = CNodeFactory.newWhileInstruction(condition);

        List<CNode> whileInstructions = new ArrayList<>();
        whileInstructions.add(whileToken);
        whileInstructions.addAll(instructions);

        CNode block = CNodeFactory.newBlock(whileInstructions);
        addInstruction(block);
    }

    /**
     * 
     */
    public void addNewline() {
        addLiteralInstruction("\n");
    }

    /**
     * Returns a map with the variables used in the current CInstructionList (including inputs and outputs).
     * 
     * @return
     */
    public Map<String, VariableType> getLocalVars() {
        Map<String, VariableType> typesMap = SpecsFactory.newHashMap();

        for (CNode instruction : get()) {

            // Get variables
            List<Variable> variables = CNodeUtils.getLocalVariables(instruction);

            // Add variable to map
            for (Variable variable : variables) {
                typesMap.put(variable.getName(), variable.getType());
            }

        }

        // Add literal variables
        for (Variable variable : getLiteralVariables()) {
            typesMap.put(variable.getName(), variable.getType());
        }

        return typesMap;
    }

    /**
     * @param readLines
     */
    public void addLiteralInstructions(List<String> instructions) {
        for (String inst : instructions) {
            addLiteralInstruction(inst);
        }

    }

    public void addBreak() {
        addInstruction(CNodeFactory.newReservedWord(ReservedWord.Break));
    }

    public void addContinue() {
        addInstruction(CNodeFactory.newReservedWord(ReservedWord.Continue));
    }

    public CNode toCNode() {
        return CNodeFactory.newBlock(get());
    }
}
