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

package org.specs.CIR.FunctionInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.specs.CIR.CirUtils;
import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.TemporaryUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.Variable;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Base class that represents a unit of C code, such as functions.
 * 
 * <p>
 * A FunctionInstance class is uniquely identified by its C name, since in C all functions must have unique names.
 * 
 * @author Joao Bispo
 * 
 */
public abstract class FunctionInstance {

    private final FunctionType functionTypes;
    private boolean built = false;

    /**
     * @param parentFunction
     * @param typeData
     */
    public FunctionInstance(FunctionType functionTypes) {
        this.functionTypes = functionTypes;
    }

    /**
     * The C code of the complete function that will appear in the implementation file (.C).
     * 
     * <p>
     * This function will try several strategies to generate the C code for the implementation file, in the following
     * order:<br>
     * - If hasImplementation() returns false, the function throws an exception;<br>
     * - If getInstructions() does not return null, builds the C code based on the instructions;<br>
     * - If none of the above apply, throws an exception;<br>
     * 
     * @return
     */
    public String getImplementationCode() {

        // Check if implementable
        if (!hasImplementation()) {
            throw new UnsupportedOperationException("FunctionInstance is not implementable.");
        }

        // Check if it has CInstructions
        CInstructionList instructions = getInstructions();
        if (instructions != null) {
            if (built) {
                throw new IllegalStateException("Can only getImplementationCode for InstructionsInstance once.");
            } else {
                built = true;
            }
            return CodeGeneratorUtils.functionImplementation(this, instructions);
        }

        // Case not defined, throw message
        String exceptionMessage = "No code generation option available for this FunctionInstance. Please, use one of the following:\n"
                + " - If the instance is not implementable (does not have a C implementation), override hasImplementation() and return false;\n"
                + " - If the instance has a custom String implementation,  override getCustomImplementation() and return a String with the C code of the function;\n"
                + " - If the body of function is completely defined with a CInstructionList object, override getInstructions() and return the instructions object;";
        throw new RuntimeException(exceptionMessage);
    }

    /**
     * The C code that will appear in the header file (.H).
     * 
     * <p>
     * As default, calls the function 'CodeGeneratorUtils.functionDeclarationCode' plus ';'
     * 
     * @return
     */
    public String getDeclarationCode() {
        StringBuilder builder = new StringBuilder();

        // Add function comments
        builder.append(CodeGeneratorUtils.getFunctionComments(getComments()));

        // Add declaration
        builder.append(CodeGeneratorUtils.functionDeclarationCode(this) + ";");

        return builder.toString();
    }

    /**
     * Returns true if it has code for a corresponding .C file.
     * 
     * <p>
     * For instance, C operators (e.g.: +, -) do not have a C implementation as a function. A FunctionImplementation for
     * functions already in the C library are another example.
     * 
     * <p>
     * As default, returns true.
     * 
     * @return
     */
    public boolean hasImplementation() {
        return true;
    }

    /**
     * Returns true if the function has code for a corresponding .H file.
     * 
     * <p>
     * As default, returns true.
     * 
     * @return
     */
    public boolean hasDeclaration() {
        return true;
    }

    /**
     * Returns the name of the function in C.
     * 
     * <p>
     * Usually the function name will need to be specialized to the input types, since C does not support functions with
     * the same name and different outputs. The method 'FunctionUtils.getTypesSuffix()' can be used to create an unique
     * ID.
     * 
     * @return
     */
    public abstract String getCName();

    /**
     * The comments that will appear before the function implementation and declaration.
     * 
     * <p>
     * As default, returns an empty list.
     * 
     * @return
     */
    public List<String> getComments() {
        return Collections.emptyList();
    }

    /**
     * Helper method with variadic arguments.
     * 
     * @param cArguments
     * @return
     */
    public String getCallCode(CNode... cArguments) {
        return getCallCode(Arrays.asList(cArguments));
    }

    /**
     * Returns the C used when calling this FunctionInstance.
     * 
     * <p>
     * As default, the code will be generated with the function 'CodeGeneratorUtils.functionCallCode', and will have the
     * format 'getCName()'('Argument1', 'Argument2', etc...)
     * 
     * @return
     * @throws CodeGenerationException
     */
    public String getCallCode(List<CNode> cArguments) {

        List<CNode> processedArgumentList = new ArrayList<>();

        for (int i = 0; i < cArguments.size(); ++i) {
            if (i >= getFunctionType().getArgumentsTypes().size() || !getFunctionType().isInputReference(i)) {
                processedArgumentList.add(cArguments.get(i));
            }
        }

        return CodeGeneratorUtils.functionCallCode(getCName(), getFunctionType().getCInputTypes(),
                processedArgumentList);
    }

    /**
     * Returns the includes needed for the declaration of the function (.H file).
     * 
     * <p>
     * They correspond to the includes necessary for the variables types in function arguments and the return type.
     * 
     * <p>
     * Always returns a set.
     * 
     * @param cmodule
     * @return
     */
    public Set<String> getDeclarationIncludes() {
        return FunctionInstanceUtils.getIncludes(getFunctionType());

    }

    /**
     * Returns a set with all the instances the H declaration depends on. Always returns a set.
     * 
     * <p>
     * This instances are collected as follows: <br>
     * - Collects the instances needed for the function input and output types;<br>
     * 
     * @param instance
     * @return
     */
    public Set<FunctionInstance> getDeclarationInstances() {
        return FunctionInstanceUtils.getFunctionTypesInstances(this.functionTypes);
        /*
        Set<FunctionInstance> codeInstances = FactoryUtils.newHashSet();
        
        if (functionTypes == null) {
        return codeInstances;
        }
        
        // Add instances of the variables present in the function signature
        for (VariableType type : functionTypes.getCInputTypes()) {
        codeInstances.addAll(CType.getInstances(type));
        }
        codeInstances.addAll(CType.getInstances(functionTypes.getCReturnType()));
        
        return codeInstances;
        */
    }

    /**
     * The name of the include needed to use this function in other files.
     * 
     * <p>
     * As default, returns the header path of the parent CModule. However, if the function does not have an include,
     * should return null.
     * 
     * @return
     */
    public String getHFilename() {
        return CirUtils.getFilename(getCFilename(), ".h");
    }

    /**
     * @return the types of this implementation
     */
    public FunctionType getFunctionType() {
        return this.functionTypes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getCName();
    }

    /**
     * If function is not implementable, can return null. Otherwise, returns the name of the file where it should be
     * implemented.
     * 
     * @return
     */
    public abstract String getCFilename();

    /**
     * Returns a set with all the CodeInstances this instance is directly dependent on.
     * 
     * <p>
     * If this instance does not depend on any other CodeInstances, the method can return null. This method is used to
     * determine which CodeInstances will be written when generating the code files.
     * 
     * <p>
     * If the code used by this method uses a CInstructionList, you can use the method
     * 'FunctionUtils.getFunctionInstances' to collect the CodeInstances.
     * 
     * @return
     */
    // public abstract Set<FunctionInstance> getInstances();

    /**
     * Returns a set with all the instances the C implementation depends on. Always returns a set.
     * 
     * <p>
     * This instances are collected as follows: <br>
     * - Collects the instances returned by getDeclarationInstances();<br>
     * - Collects the instances inside the instructions returned by getInstructions();<br>
     * - Collects the instances defined in getCustomInstances();<br>
     * 
     * 
     * @return
     */
    public Set<FunctionInstance> getImplementationInstances() {
        Set<FunctionInstance> instances = SpecsFactory.newHashSet();

        // Add declaration instances
        SpecsFactory.addAll(instances, getDeclarationInstances());
        // FactoryUtils.addAll(instances,
        // FunctionUtils.getDeclarationInstances(this.getFunctionTypes()));

        // Add instruction instances
        SpecsFactory.addAll(instances, FunctionInstanceUtils.getInstrucionsInstances(getInstructions()));
        // Set<FunctionInstance> instances = FunctionUtils.getInstances(this, instructions);

        // Add custom instances
        // FactoryUtils.addAll(instances, getCustomImplementationInstances());

        /*
        if (getCustomInstances() != null) {
        System.out.println("FUNC:" + getCName() + "\nADDING CUSTOM:" + getCustomInstances());
        }
        */

        return instances;
    }

    /**
     * The instances needed to use this function when called/used inside another instance.
     * 
     * <p>
     * As default, this method returns a set with a reference to itself. However, there are cases (e.g., inline
     * functions) which might need to return other instances, e.g., calls to functions inlined code uses.
     * 
     * @return
     */
    public Set<FunctionInstance> getCallInstances() {
        /*
        if (!hasImplementation()) {
        return null;
        }
        */

        Set<FunctionInstance> instances = SpecsFactory.newHashSet();
        instances.add(this);
        return instances;
    }

    /**
     * The includes needed to use this function when called/used inside another instance.
     * 
     * <p>
     * As default, this method returns a set with its selfInclude. However, there are cases (e.g., inline functions)
     * which might need to return other includes, for instance, macros they need that are not covered by other
     * instances.
     * 
     * @return
     */
    public Set<String> getCallIncludes() {
        Set<String> includes = SpecsFactory.newHashSet();

        if (getHFilename() != null) {
            includes.add(getHFilename());
        }

        return includes;
    }

    /**
     * Returns true if the implementation becomes specialized when given constant inputs.
     * 
     * <p>
     * As default, returns false.
     * 
     * @return
     */
    public boolean isConstantSpecialized() {
        return false;
    }

    /**
     * Returns true if the implementation does not implement a function, but directly inserts code.
     * 
     * @return
     */
    public boolean isInlined() {
        return false;
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param cArguments
     * @return
     */
    public FunctionCallNode newFunctionCall(CNode... cArguments) {
        return newFunctionCall(Arrays.asList(cArguments));
    }

    /**
     * Creates a new function call token of this function, for the given arguments.
     * 
     * <p>
     * If checkCallInputs() returns true, calls CTokenUtils.parseFuctionCallArguments, which can change the given list
     * of arguments.
     * 
     * <p>
     * If the number of function inputs - number of input arguments is the same as the number of outputs-as-inputs,
     * automatically adds temporary input tokens for them.
     * 
     * 
     * @param cArguments
     *            can be null
     * @return
     */
    public FunctionCallNode newFunctionCall(List<CNode> cArguments) {

        // In case arguments is a null list
        if (cArguments == null) {
            cArguments = SpecsFactory.newArrayList();
        }

        // Check if outputs-as-inputs are missing from the list of arguments
        cArguments = fixArguments(cArguments);

        // Check function call
        if (checkCallInputs()) {
            CNodeUtils.parseFunctionCallArguments(getCName(), getFunctionType().getFullCInputTypes(), cArguments);
        }

        cArguments = SpecsFactory.getUnmodifiableList(cArguments);

        // Create function call
        FunctionCallNode functionCall = CNodeFactory.newFunctionCall(this, cArguments);

        return functionCall;
    }

    private List<CNode> fixArguments(List<CNode> cArguments) {
        FunctionType fType = getFunctionType();

        int missingArgs = fType.getArgumentsTypes().size() + fType.getOutputAsInputTypes().size() - cArguments.size();
        if (missingArgs == 0) {
            return cArguments;
        }

        // Check the number of missing arguments are the same as the number of outs-as-in
        if (fType.getNumOutsAsIns() == missingArgs) {
            cArguments = TemporaryUtils.updateInputsWithOutsAsIns(getFunctionType(), cArguments);
        }

        return cArguments;
    }

    /**
     * TODO Returns a set with all the instances the current instance is directly dependent on.
     * 
     * <p>
     * This instances are collected as follows: <br>
     * - If a CInstructionList is defined for this Instance, collects its instances;<br>
     * - Collects the instances defined in customInstances();<br>
     * 
     * @return
     */

    /**
     * Returns all the includes needed for the .C implementation. Always returns a set.
     * 
     * <p>
     * The includes are collected as follows: <br>
     * - Collects the includes needed for the instances returned by getInstances();<br>
     * - Collects the includes needed by the instructions returned by getInstructions();<br>
     * 
     * @return
     */
    public Set<String> getImplementationIncludes() {

        Set<String> includes = SpecsFactory.newHashSet();

        // Add instance includes
        // FactoryUtils.addAll(includes, FunctionUtils.collectInstancesIncludes(this));
        SpecsFactory.addAll(includes,
                FunctionInstanceUtils.collectInstancesIncludes(getHFilename(), getImplementationInstances()));

        // Add instructions includes
        SpecsFactory.addAll(includes, FunctionInstanceUtils.collectInstructionsIncludes(getInstructions()));

        // Add custom includes
        // FactoryUtils.addAll(includes, getCustomImplementationIncludes());

        return includes;
    }

    /**
     * If the FunctionInstance uses an instruction list, override this function and return the instructions through it.
     * 
     * <p>
     * If this function returns the list of CToken instructions, the tools can perform additional work, such as
     * automatically extracting the implementation includes.
     * 
     * <p>
     * As default, returns null.
     * 
     * @return
     */
    protected CInstructionList getInstructions() {
        return null;
    }

    /**
     * Indicates if the type of inputs should be maintained, if any of the inputs of the function is a literal.
     * 
     * <p>
     * As default, returns true.
     * 
     * @return
     */
    public boolean maintainLiteralTypes() {
        return true;
    }

    /**
     * If true, checks if the input arguments in a FunctionCall are compatible with the types defined in FunctionInputs.
     * 
     * <p>
     * As default, returns true.
     * 
     * @return
     */
    protected boolean checkCallInputs() {
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCName() == null) ? 0 : getCName().hashCode());
        return result;
    }

    /**
     * A FunctionInstance class is uniquely identified by its C name, since in C all functions must have unique names.
     * 
     */
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FunctionInstance other = (FunctionInstance) obj;
        if (getCName() == null) {
            if (other.getCName() != null) {
                return false;
            }
        } else if (!getCName().equals(other.getCName())) {
            return false;
        }
        return true;
    }

    /**
     * Variables that might be needed for declaration when calling this function.
     * 
     * <p>
     * For instance, this method is needed for inlined functions that declare new variables.
     * 
     * <p>
     * As default returns an empty list.
     * 
     * @return
     */
    public Collection<Variable> getCallVars() {
        return Collections.emptyList();
    }

    public PrecedenceLevel getCallPrecedenceLevel() {
        return PrecedenceLevel.Unspecified;
    }

    public Optional<String> getAssignmentCallCode(List<CNode> arguments) {
        return Optional.empty();
    }

}
