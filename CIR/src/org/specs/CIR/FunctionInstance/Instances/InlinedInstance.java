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

package org.specs.CIR.FunctionInstance.Instances;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.providers.KeyStringProvider;

/**
 * General instance for functions that do not have implementation (e.g., a+b, functions already defined elsewhere...).
 * This class insert custom code in the place of a function call.
 * 
 * <p>
 * To use it you should create an instance of InlineCode (e.g., as an anonymous class) with the code that should be used
 * when the function is called.
 * 
 * <p>
 * InlinedInstances are a bit special, some of the information (e.g., callVars) might be only known after getCallCode()
 * is called (since at that moment, the instance is specialized to a given set of input CTokens).
 * 
 * <p>
 * This instance does not have implementation.
 * 
 * @author Joao Bispo
 * 
 */
public class InlinedInstance extends FunctionInstance {

    private final String functionName;
    private final InlineCode inlineCode;
    private PrecedenceLevel precedenceLevel = PrecedenceLevel.Unspecified;

    // private Set<String> customImplementationIncludes;
    private Set<String> customCallIncludes;
    private Set<FunctionInstance> callInstances;
    private Set<Variable> callVariables;
    private String selfInclude;
    private Boolean checkInputs;

    private boolean maintainLiteralTypes;

    /**
     * Creates a FunctionImplementation representing direct code, instead of a function call (e.g., a+b).
     * 
     * @param functionTypes
     * @param functionName
     */
    public InlinedInstance(FunctionType functionTypes, String functionName, InlineCode inlineCode) {
        super(functionTypes);

        this.functionName = functionName;
        this.inlineCode = inlineCode;

        // customImplementationIncludes = null;
        customCallIncludes = null;
        callInstances = null;
        selfInclude = null;
        checkInputs = null;
        callVariables = null;

        maintainLiteralTypes = super.maintainLiteralTypes();
    }

    public InlinedInstance(FunctionType functionTypes, String functionName, CInstructionList instructions) {
        this(functionTypes, functionName, InlineCode.newInstance(instructions));

        setInstructionsInfo(instructions);
    }

    /**
     * @param customImplementationIncludes
     *            the includesDeclaration to set
     */
    public void setCustomImplementationIncludes(String... includes) {
        Set<String> includesImplementation = SpecsFactory.newHashSet(Arrays.asList(includes));
        // this.includesImplementation = includesDeclaration;
        setCustomImplementationIncludes(includesImplementation);
    }

    public void setCustomImplementationIncludes(Set<String> includes) {
        // LoggingUtils
        // .msgWarn("This instance has no implementation, check if you should be calling this or
        // 'setCustomCallIncludes'");
        throw new RuntimeException(
                "InlinedInstance has no implementation, check if you should be calling 'setCustomCallIncludes'");
        // this.customImplementationIncludes = includes;
    }

    /*
    public void setCustomCallIncludes(List<StringProvider> callIncludes) {
    List<String> strings = StringProvider.toList(callIncludes);
    }
    */

    public void setCustomCallIncludes(KeyStringProvider... callIncludes) {
        setCustomCallIncludes(SpecsFactory.newHashSet(KeyStringProvider.toList(callIncludes)));
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param callIncludes
     */
    public void setCustomCallIncludes(String... callIncludes) {

        Set<String> customIncludes = SpecsFactory.newHashSet();
        for (String callInclude : callIncludes) {
            customIncludes.add(callInclude);
        }

        setCustomCallIncludes(customIncludes);
    }

    /**
     * Add includes used when instance is called.
     * 
     * @param callIncludes
     */
    public void setCustomCallIncludes(Set<String> callIncludes) {
        customCallIncludes = callIncludes;
    }

    public void setCallInstances(FunctionInstance... dependentInstances) {
        setCallInstances(Sets.newHashSet(dependentInstances));
    }

    /**
     * @param dependentInstances
     *            the dependentInstances to set
     */
    public void setCallInstances(Set<FunctionInstance> dependentInstances) {
        callInstances = dependentInstances;
    }

    /**
     * Returns false.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.specs.CIR.Function.SpecializedFunction#isImplementable()
     */
    @Override
    public boolean hasImplementation() {
        return false;
    }

    /**
     * Returns false.
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#hasDeclaration()
     */
    @Override
    public boolean hasDeclaration() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.specs.CIR.Function.SpecializedFunction#getCallCode()
     */
    @Override
    // public abstract String getFunctionCallCode(List<CToken> arguments);
    public String getCallCode(List<CNode> arguments) {
        Preconditions.checkArgument(arguments != null);

        String code = inlineCode.getInlineCode(arguments);

        // If code is not multi-line, just return
        if (!code.trim().contains("\n")) {
            return code;
        }

        // If code is multi-line, add comment and additional line after
        StringBuilder builder = new StringBuilder();
        builder.append("// Inlined '" + getCName() + "'\n");
        builder.append(code);

        // Add a new line, if it does not have already two new lines
        if (!code.endsWith("\n\n")) {
            builder.append("\n");
        }

        return builder.toString();

    }

    /**
     * @deprecated
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.specs.CIR.Function.SpecializedFunction#getBodyIncludes()
     */
    /*
    @Override
    public Set<String> getCustomImplementationIncludes() {
    return customImplementationIncludes;
    }
    */

    /**
     * Besides the includes collection originally by FunctionInstance implementation, it also collects:<br>
     * - the includes defined by setCustomImplementationIncludes();<br>
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationIncludes()
     */
    @Override
    public Set<String> getImplementationIncludes() {
        throw new RuntimeException(
                "InlinedInstance has no implementation, check if you should be calling 'getCallIncludes'");
        /*
        	Set<String> includes = super.getImplementationIncludes();
        
        	FactoryUtils.addAll(includes, customImplementationIncludes);
        
        	return includes;
        	*/
    }

    /**
     * As default is null, but can be set with setSelfInclude.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.specs.CIR.Function.SpecializedFunction#getCorrespondingInclude()
     */
    @Override
    public String getHFilename() {
        return selfInclude;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionImplementation#getCFileName()
     */
    @Override
    public String getCFilename() {
        return "CUSTOM_" + functionName;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationInstances()
     */
    /*
    @Override
    public Set<FunctionInstance> getImplementationInstances() {
    Set<FunctionInstance> instances = super.getImplementationInstances();
    
    FactoryUtils.addAll(instances, dependentInstances);
    
    return instances;
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getCustomInstances()
     */
    /*
    @Override
    protected Set<FunctionInstance> getCustomImplementationInstances() {
    return dependentInstances;
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionImplementation#getCName()
     */
    @Override
    public String getCName() {
        return functionName;
    }

    /**
     * Since this function is not implementable and will be inlined, returns the instances added through
     * setCallInstances.
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getSelfInstances()
     */
    @Override
    public Set<FunctionInstance> getCallInstances() {
        if (callInstances == null) {
            return Collections.emptySet();
        }

        return callInstances;
        // return getCustomImplementationInstances();
        // return getImplementationInstances();
    }

    /**
     * Sets the include needed to use this function.
     * 
     * <p>
     * Can be useful when class is used to represent functions already defined elsewhere.
     * 
     * @param selfInclude
     *            the selfInclude to set
     */
    public void setSelfInclude(String selfInclude) {
        this.selfInclude = selfInclude;
    }

    /**
     * @param checkInputs
     *            the checkInputs to set
     */
    public void setCheckCallInputs(Boolean checkInputs) {
        this.checkInputs = checkInputs;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#checkCallInputs()
     */
    @Override
    protected boolean checkCallInputs() {
        if (checkInputs == null) {
            return super.checkCallInputs();
        }

        return checkInputs;
    }

    @Override
    public Set<String> getCallIncludes() {
        Set<String> callIncludes = super.getCallIncludes();

        SpecsFactory.addAll(callIncludes, customCallIncludes);

        return callIncludes;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#isInlined()
     */
    @Override
    public boolean isInlined() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#maintainLiteralTypes()
     */
    @Override
    public boolean maintainLiteralTypes() {
        return maintainLiteralTypes;
    }

    /**
     * @param maintainLiteralTypes
     *            the maintainLiteralTypes to set
     */
    public void setMaintainLiteralTypes(boolean maintainLiteralTypes) {
        this.maintainLiteralTypes = maintainLiteralTypes;
    }

    @Override
    public Collection<Variable> getCallVars() {
        if (callVariables == null) {
            return super.getCallVars();
        }

        return callVariables;
    }

    public void setCallVars(Variable... callVars) {
        setCallVars(Arrays.asList(callVars));
    }

    public void setCallVars(Collection<Variable> callVars) {
        callVariables = Sets.newHashSet(callVars);
    }

    public void setCallPrecedenceLevel(PrecedenceLevel precedenceLevel) {
        this.precedenceLevel = precedenceLevel;
    }

    @Override
    public PrecedenceLevel getCallPrecedenceLevel() {
        return precedenceLevel;
    }

    public void setInstructionsInfo(CInstructionList instructions) {
        // Set called variables, so that they are instantiated
        setCallVars(FunctionInstanceUtils.getFunctionVariables(instructions, Collections.emptyList()));
    }

}
