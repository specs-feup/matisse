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

package org.specs.CIR.Language.Operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.ParenthesisNode;
import org.specs.CIR.TypesOld.CNumber;
import org.suikasoft.MvelPlus.MvelSolver;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class COperatorSolver {

    // private static final Set<CNodeType> BYPASS_SET = EnumSet.of(CNodeType.Parenthesis);
    private static final List<Class<? extends CNode>> BYPASS_SET = Arrays.asList(ParenthesisNode.class);

    /**
     * @param op
     * @param inputNumbers
     * @param invertArgs
     * @return
     */
    public static Object solve(COperator op, List<CNode> inputNumbers, boolean invertArgs) {

	// Transform the C operator into something that can be solver by the current solver

	if (op == COperator.UnaryPlus) {
	    inputNumbers.add(0, CNodeFactory.newCNumber(0));
	    return solveHelper(COperator.Addition, inputNumbers, invertArgs);
	}

	if (op == COperator.UnaryMinus) {
	    inputNumbers.add(0, CNodeFactory.newCNumber(0));
	    return solveHelper(COperator.Subtraction, inputNumbers, invertArgs);
	}

	if (op == COperator.LogicalOr) {
	    List<CNode> newInputs = fromNumberToLogical(inputNumbers);
	    return solveHelper(op, newInputs, invertArgs);
	}

	if (op == COperator.LogicalAnd) {
	    List<CNode> newInputs = fromNumberToLogical(inputNumbers);
	    return solveHelper(op, newInputs, invertArgs);
	}

	if (op == COperator.LogicalNegation) {
	    List<CNode> newInputs = fromNumberToLogical(inputNumbers);
	    return solveHelper(op, newInputs, invertArgs);
	}

	return null;
    }

    /**
     * @param inputNumbers
     * @return
     */
    private static List<CNode> fromNumberToLogical(List<CNode> inputNumbers) {
	List<CNode> newTokens = SpecsFactory.newArrayList();

	for (CNode input : inputNumbers) {
	    CNumber number = ((CNumberNode) input.normalize(COperatorSolver.BYPASS_SET)).getCNumber();

	    Long value = number.getNumber().longValue();

	    CNode logicalValue;
	    // if(number.getLong() != 0) {
	    if (value != 0) {
		logicalValue = CNodeFactory.newLiteral("true");
	    } else {
		logicalValue = CNodeFactory.newLiteral("false");
	    }

	    newTokens.add(logicalValue);
	}

	return newTokens;
    }

    /**
     * @param op
     * @param inputNumbers
     * @param invertArgs
     * @return
     */
    private static Object solveHelper(COperator op, List<CNode> inputNumbers, boolean invertArgs) {
	// Get C code expression
	String expression = getCode(op, inputNumbers, invertArgs);

	// Evaluate expression
	Object result = MvelSolver.eval(expression);

	return result;
    }

    public static String getCode(COperator op, List<CNode> arguments, boolean invertArgs) {
	if (invertArgs) {
	    if (arguments.size() != 2) {
		SpecsLogs.warn("Number of arguments is diff than 2 (" + arguments.size()
			+ "). Check if there is no problem.");
	    }

	    arguments = new ArrayList<>(arguments);
	    Collections.reverse(arguments);
	}

	return op.getCode(arguments);
    }
}
