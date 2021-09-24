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

package org.specs.CIRFunctions.Utilities;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class UtilityInstances extends CirBuilder {

    public UtilityInstances(ProviderData data) {
	super(data);
    }

    /**
     * <p>
     * Inputs:<br>
     * - The node to cast.
     * 
     * @param targetType
     * @return
     */
    public static InstanceProvider getCastToScalarProvider(VariableType targetType) {
	checkArgument(targetType != null, "targetType must not be null");

	CirInputsChecker checker = new CirInputsChecker()
		.numOfInputs(1);

	return new GenericInstanceProvider(checker, data -> newCastToScalar(data.getInputTypes().get(0), targetType));
    }

    /**
     * Dereferences an expression. Assumes that input type is a pointer
     * 
     * <p>
     * Inputs: <br>
     * - A PointerType, which will be dereferenced;<br>
     * 
     * @return
     */
    public static InstanceProvider getDereferenceProvider() {
	CirInputsChecker checker = new CirInputsChecker()
		.numOfInputs(1);

	return new GenericInstanceProvider(checker, data -> newDereference(data));
    }

    /**
     * Creates a new instance of the function 'sign', which returns an integer representing the sign of the given
     * number. Returns 1 if the number is positive, -1 if the number is negative and 0, if the number is zero.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A numeric type with the number to be tested;
     * 
     * @return
     */
    FunctionInstance newSignInstance() {

	// Name of the function
	String functionName = "sign";

	// Input names
	String inputName = "number";

	List<String> inputNames = Arrays.asList(inputName);

	// Input types
	// List<VariableType> inputTypes = Arrays.asList(VariableTypeFactory.newDouble());
	List<VariableType> inputTypes = Arrays.asList(getNumerics().newDouble());

	// FunctionTypes
	FunctionType fTypes = FunctionType.newInstance(inputNames, inputTypes, null, getNumerics().newInt());

	String cBody = SpecsIo.getResource(UtilityResource.SIGN_BODY.getResource());

	LiteralInstance copy = new LiteralInstance(fTypes, functionName, UtilityResource.getLibFilename(), cBody);

	return copy;
    }

    private static FunctionInstance newDereference(ProviderData data) {

	// Get original input type, we need to know if it is a pointer type
	VariableType inputType = data.getOriginalInputTypes().get(0);

	String functionName = "dereference_" + inputType.getSmallId();

	// Input type is assumed to be a pointer
	// VariableType inputTypePointer = inputType.pointer().getType(true);
	// Output type is input type no longer a pointer
	// VariableType outputType = inputType.pointer().getType(false);
	// Output type is never a pointer
	VariableType outputType = inputType.pointer().getType(false);

	FunctionType ftypes = FunctionType.newInstanceNotImplementable(Arrays.asList(inputType), outputType);

	InlineCode code = arguments -> {
	    CNode arg = arguments.get(0);

	    return arg.getCode();
	};

	InlinedInstance inlinedInstance = new InlinedInstance(ftypes, functionName, code);

	return inlinedInstance;
    }

    /**
     * Creates a new instance of the function 'cast', which writes a cast for the given argument.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - The token to be cast;
     * <p>
     * Instance creation receives:<br>
     * - The type to be cast to;
     * 
     * @param targetType
     * @return
     */
    public static FunctionInstance newCastToScalar(VariableType inputType, VariableType targetType) {

	// Check if input type has constant
	String constantString = ScalarUtils.getConstantString(inputType);

	// Remove pointer information
	// targetType = PointerUtils.getTypeWithoutPointer(targetType);

	// If has constant, verify that the number fits the type
	if (constantString != null) {
	    if (!ScalarUtils.getScalar(targetType).testRange(constantString)) {
		throw new RuntimeException("Cannot cast type with constant '"
			+ constantString + "' to a '" + targetType + "'");
	    }
	}

	// Build output type
	final ScalarType outputType = ScalarUtils.toScalar(ScalarUtils.propagateConstant(inputType, targetType));

	// Build name of function
	String functionName = "cast" + inputType.getSmallId() + "_to_" + outputType.getSmallId();
	if (constantString != null) {
	    functionName += "_" + constantString;
	}

	final FunctionType types = FunctionType.newInstanceNotImplementable(Arrays.asList(inputType), outputType);

	InlineCode inlineCode = new InlineCode() {

	    @Override
	    public String getInlineCode(List<CNode> arguments) {

		CNode argument = arguments.get(0);

		ScalarType sourceType = ScalarUtils.toScalar(argument.getVariableType());

		// CToken number = CTokenUtils.getToken(argument, CTokenType.CNumber);
		// System.out.println("CAST OF '" + argument.getCode() + "' to " + outputType + " (type:"
		// + argument + ")");
		// System.out.println("HAS CONSTANT?" + ScalarUtils.toScalar(sourceType).scalar().getConstantString());
		// System.out.println("NUMBER:" + number);
		// If token is a CNumber create new token with output type and return corresponding code
		if (argument instanceof CNumberNode) {
		    // if (number != null) {
		    CNumber cnumber = ((CNumberNode) argument).getCNumber();

		    return CNodeFactory.newCNumber(cnumber.getNumber(), outputType).getCode();
		}

		String declarationCode = CodeUtils.getType(outputType);
		StringBuilder builder = new StringBuilder();

		// builder.append("((" + declarationCode + ") ");
		builder.append("(" + declarationCode + ") ");

		String prefix = UtilityInstances.discoverPrefix(sourceType, outputType);
		builder.append(prefix);

		/*
		System.out.println("PREFIX:" + prefix);
		System.out.println("GET CODE:" + arguments.get(0).getCode());
		if (arguments.get(0).getCode().startsWith("*")) {
		    System.out.println("FOUND * on " + arguments.get(0).getClass());
		}
		*/
		CNode argumentNode = arguments.get(0);
		String contentCode = argumentNode.getCode();
		// HACK
		if (contentCode.startsWith("*")) {
		    contentCode = contentCode.substring(1);
		}

		if (PrecedenceLevel.requireContentParenthesis(PrecedenceLevel.PrefixIncrement,
			argumentNode.getPrecedenceLevel())) {
		    contentCode = "(" + contentCode + ")";
		}
		builder.append(contentCode);

		return builder.toString();
	    }

	};

	InlinedInstance instance = new InlinedInstance(types, functionName, inlineCode);
	instance.setCallPrecedenceLevel(PrecedenceLevel.PrefixIncrement);

	// instance.setCustomCallIncludes(outputType.getIncludes());
	// instance.setCustomCallIncludes(CodeUtils.getIncludes(outputType));
	instance.setCustomCallIncludes(outputType.code().getIncludes());

	return instance;
    }

    /**
     * Discovers the right prefix, according to pointer information.
     * 
     * <p>
     * - If both are pointers, or none are pointers, returns empty string; <br>
     * - If source is pointer but target is not pointer, returns "*"; <br>
     * - If target is pointer but source is not pointer, returns "&"; <br>
     * 
     * 
     * @param cToken
     * @param outputType
     * @return
     */
    private static String discoverPrefix(VariableType source, VariableType target) {
	boolean sourceIsPointer = ReferenceUtils.isPointer(source);
	boolean targetIsPointer = ReferenceUtils.isPointer(target);

	if (sourceIsPointer == targetIsPointer) {
	    return "";
	}

	if (sourceIsPointer && !targetIsPointer) {
	    return "*";
	}

	if (!sourceIsPointer && targetIsPointer) {
	    return "&";
	}

	throw new RuntimeException("Should not be able to reach this point");
    }

}
