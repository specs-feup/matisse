/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Utilities.CodeReplacer;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.CreateFromMatrix;
import org.specs.CIRTypes.Types.Pointer.PointerType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

/**
 * Implements the function sort(matrix, order)
 *
 */
public class SortBuilder implements InstanceProvider {
    private static final String FILE_NAME = "lib/sort";

    private SortBuilder() {
    }

    public static InstanceProvider newSortDefaultOrderBuilderForOneOutput() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(1)
		.isMatrix(0)
		.numOfOutputsAtMost(1);

	return new GenericInstanceProvider(checker, MFileProvider.getProviderWithLibraryDependencies(
		BaseResource.SORT1, SystemInclude.Stdlib.getIncludeName()));
    }

    public static InstanceProvider newSortDefaultOrderBuilderForTwoOutputs() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(1)
		.isMatrix(0)
		.numOfOutputs(2);

	return new GenericInstanceProvider(checker, MFileProvider.getProviderWithLibraryDependencies(
		BaseResource.SORT2, SystemInclude.Stdlib.getIncludeName()));
    }

    public static InstanceProvider newSortBuilder() {
	MatisseChecker checker = new MatisseChecker()
		.numOfInputs(2)
		.isMatrix(0)
		.isString(1);

	return new GenericInstanceProvider(checker, new SortBuilder());
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	return new SortBuilderImpl(data).create();
    }

    private static class SortBuilderImpl extends AInstanceBuilder {

	public SortBuilderImpl(ProviderData data) {
	    super(data);
	}

	@Override
	public FunctionInstance create() {
	    List<String> inputNames = Arrays.asList("matrix", "order");
	    List<VariableType> inputTypes = getData().getInputTypes();
	    MatrixType returnType = getData().getInputType(MatrixType.class, 0);
	    StringType orderType = getData().getInputType(StringType.class, 1);
	    String outputMatrix = "sorted_matrix";
	    String indexMatrix = "index_matrix";
	    boolean fillIndexMatrix;
	    List<String> outputNames;
	    List<VariableType> originalOutputTypes = getData().getOutputTypes();
	    List<VariableType> outputTypes = new ArrayList<>();
	    outputTypes.add(returnType);
	    ScalarType indexElementType = getNumerics().newInt();
	    MatrixType indexMatrixType = returnType.matrix().setElementType(indexElementType);

	    VariableType outputType = originalOutputTypes.isEmpty() ? null : originalOutputTypes.get(0);
	    if (outputType != null && !outputType.equals(returnType)) {
		throw new CodeGenerationException(
			"Mismatched types in sort function. First output and first argument must have the same type: got "
				+ outputType + ", " + returnType);
	    }

	    int numOutputs = getData().getNargouts().orElse(1);

	    switch (numOutputs) {
	    case 0:
		throw new CodeGenerationException("sort must have at least 1 output");
	    case 1:
		outputNames = Arrays.asList(outputMatrix);
		fillIndexMatrix = false;
		break;
	    case 2:
		outputNames = Arrays.asList(outputMatrix, indexMatrix);
		outputTypes.add(indexMatrixType);
		fillIndexMatrix = true;
		break;
	    default:
		throw new CodeGenerationException("Too many outputs");
	    }

	    String order = orderType.getString();
	    boolean ascending;
	    if (order.equalsIgnoreCase("ascend")) {
		ascending = true;
	    } else if (order.equalsIgnoreCase("descend")) {
		ascending = false;
	    } else {
		throw new CodeGenerationException("Second argument of sort must be 'ascend' or 'descend'.");
	    }

	    String functionName = "sort_" + outputTypes.size() + (ascending ? "a" : "d") + returnType.getSmallId();
	    FunctionType functionType = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes,
		    outputNames, outputTypes);

	    CInstructionList body = new CInstructionList(functionType);

	    body.addComment("We know that the order is '" + order.toLowerCase()
		    + "', so there is no need to check at runtime.");

	    CNode inputMatrixNode = CNodeFactory.newVariable(inputNames.get(0), returnType);
	    CNode sortedMatrixNode = CNodeFactory
		    .newVariable(outputMatrix, functionType.getOutputAsInputTypes().get(0));
	    CNode indexMatrixNode = CNodeFactory.newVariable(indexMatrix, indexMatrixType);

	    InstanceProvider allocProvider = returnType.matrix().functions().createFromMatrix();
	    ProviderData allocData = getData().create(returnType);
	    if (!allocProvider.getType(allocData).isNoOp()) {
		FunctionInstance sortAllocInstance = allocProvider.newCInstance(allocData);

		body.addFunctionCall(sortAllocInstance,
			Arrays.asList(inputMatrixNode, sortedMatrixNode));

		if (fillIndexMatrix) {
		    FunctionInstance indexAlloc = new CreateFromMatrix(allocData, indexElementType).create();
		    body.addFunctionCall(indexAlloc, Arrays.asList(inputMatrixNode, indexMatrixNode));
		}
	    }

	    body.addInstruction(getFunctionCall(returnType.matrix().functions().copy(), inputMatrixNode,
		    sortedMatrixNode));

	    if (fillIndexMatrix) {
		return newIndexedImplementation(returnType, indexMatrixType, functionName, body, sortedMatrixNode,
			indexMatrixNode, ascending);
	    }
	    return newSimpleImplementation(returnType, functionName, body, sortedMatrixNode, ascending);
	}

	private FunctionInstance newSimpleImplementation(MatrixType returnType, String functionName,
		CInstructionList body, CNode sortedMatrixNode, boolean ascending) {
	    CNode sortedMatrixDataNode = getFunctionCall(returnType.matrix().functions().data(), sortedMatrixNode);
	    CNode sortedMatrixSizeNode = getFunctionCall(returnType.matrix().functions().numel(), sortedMatrixNode);
	    String elementType = returnType.matrix().getElementType().code().getSimpleType();

	    String comparerFunctionName = functionName + "_comparer";

	    body.addLiteralInstruction("qsort(" + sortedMatrixDataNode.getCode() + ", " +
		    sortedMatrixSizeNode.getCode() +
		    ", sizeof(" + elementType + "), " +
		    comparerFunctionName + ");");

	    InstructionsInstance instance = new InstructionsInstance(functionName, SortBuilder.FILE_NAME, body);

	    FunctionInstance comparerFunction = getComparerFunction(comparerFunctionName, elementType, ascending);

	    instance.setCustomIncludes(SystemInclude.Stdlib.getIncludeName());
	    instance.setCustomImplementationInstances(comparerFunction);

	    return instance;
	}

	private FunctionInstance getComparerFunction(String comparerFunctionName, String elementTypeName,
		boolean ascending) {
	    VariableType voidPtr = new PointerType(new VoidType(), true);
	    VariableType returnType = getNumerics().newInt();

	    List<String> inputNames = Arrays.asList("raw1", "raw2");
	    List<VariableType> inputTypes = Arrays.asList(voidPtr, voidPtr);

	    FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, null, returnType);

	    StringBuilder body = new StringBuilder();
	    body.append("\t" + elementTypeName + " elem1 = *((const " + elementTypeName + "*)raw1);\n");
	    body.append("\t" + elementTypeName + " elem2 = *((const " + elementTypeName + "*)raw2);\n");
	    body.append('\n');

	    body.append("\tif (elem1 " + (ascending ? "<" : ">") + " elem2) return -1;\n");
	    body.append("\treturn elem1 " + (ascending ? ">" : "<") + " elem2;\n");

	    return new LiteralInstance(functionType, comparerFunctionName, SortBuilder.FILE_NAME, body.toString());
	}

	private FunctionInstance newIndexedImplementation(MatrixType returnType,
		MatrixType indexMatrixType,
		String functionName, CInstructionList body, CNode sortedMatrixNode, CNode indexMatrixNode,
		boolean ascending) {

	    CNode sortedMatrixSizeNode = getFunctionCall(returnType.matrix().functions().numel(), sortedMatrixNode);

	    VariableType intType = getNumerics().newInt();
	    VariableNode inductionVar = CNodeFactory.newVariable("i", intType);
	    CNode setIndexPosition = getFunctionCall(indexMatrixType.matrix().functions().set(), indexMatrixNode,
		    inductionVar,
		    getFunctionCall(COperator.Addition.getProvider(), inductionVar, CNodeFactory.newCNumber(1)));

	    body.addInstruction(new ForNodes(getData()).newForLoopBlock(inductionVar, sortedMatrixSizeNode,
		    setIndexPosition));

	    String sortFunctionName = functionName + "_dosort";
	    FunctionInstance sortFunction = getSortFunction(sortFunctionName, returnType, indexMatrixType, ascending);
	    body.addFunctionCall(sortFunction, sortedMatrixNode, indexMatrixNode, CNodeFactory.newCNumber(0),
		    getFunctionCall(COperator.Subtraction, sortedMatrixSizeNode, CNodeFactory.newCNumber(1)));

	    InstructionsInstance instance = new InstructionsInstance(functionName, SortBuilder.FILE_NAME, body);
	    instance.setCustomImplementationInstances(sortFunction);
	    return instance;
	}

	private FunctionInstance getSortFunction(String sortFunctionName, MatrixType sortedMatrixType,
		MatrixType indexMatrixType, boolean ascending) {

	    List<String> inputNames = Arrays.asList("dataMatrix", "indexMatrix", "begin", "end");
	    VariableType intType = getNumerics().newInt();
	    List<VariableType> inputTypes = Arrays.asList(sortedMatrixType, indexMatrixType, intType, intType);

	    FunctionType functionType = FunctionType.newInstance(inputNames, inputTypes, null, new VoidType());

	    CNode sortedMatrixNode = CNodeFactory.newVariable("dataMatrix", sortedMatrixType);
	    CNode indexMatrixNode = CNodeFactory.newVariable("indexMatrix", sortedMatrixType);

	    CNode sortedMatrixDataNode = getFunctionCall(sortedMatrixType.matrix().functions().data(),
		    sortedMatrixNode);
	    CNode indexMatrixDataNode = getFunctionCall(sortedMatrixType.matrix().functions().data(), indexMatrixNode);

	    String elementType = sortedMatrixType.matrix().getElementType().code().getType();

	    CodeReplacer replacer = new CodeReplacer(SortResource.DOSORT)
		    .replace("<DATA_TYPE>", elementType)
		    .replace("<DATA>", sortedMatrixDataNode)
		    .replace("<INDEX>", indexMatrixDataNode)
		    .replace("<FUNCTION_NAME>", sortFunctionName)
		    .replace("<COMPARISON_BEFORE>", ascending ? "<" : ">")
		    .replace("<COMPARISON_AFTER>", ascending ? ">" : "<");

	    return new LiteralInstance(functionType, sortFunctionName, SortBuilder.FILE_NAME, replacer);
	}
    }

}
