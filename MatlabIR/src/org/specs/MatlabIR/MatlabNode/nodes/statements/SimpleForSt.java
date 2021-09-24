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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * A For statement.
 * <p>
 * *
 * <p>
 * - The first child is the reserved word 'for' <br>
 * - The second child is the for expression which should have the following format: <br>
 * -- The first child is the identifier <br>
 * -- The second child is the assignment token <br>
 * -- The third child is an expression <br>
 * 
 * <p>
 * TODO: Simply structure, use structure similar to http://www.mathworks.com/help/matlab/ref/for.html<br>
 * - The first child is an identifier, representing the index <br>
 * - The second child represents the values, which can be: initVal:endVal, initVal:step:endVal and valArray (a matrix,
 * which will assign a column to the index at each iteration)
 * 
 * @author JoaoBispo
 *
 */
public class SimpleForSt extends ForSt {

    SimpleForSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    SimpleForSt(int lineNumber, List<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    SimpleForSt(int lineNumber, MatlabNode index, MatlabNode expression) {
	this(lineNumber, Arrays.asList(index, expression));

    }

    /*
    static ForSt newInstance(int lineNumber, List<MatlabNode> children) {
    IdentifierNode identifier = children.get(1).getChild(IdentifierNode.class, 0);
    MatlabNode expression = children.get(1).getChild(IdentifierNode.class, 2);
    
    return new ForSt(lineNumber, identifier, expression);
    }
    */

    /*
    private static List<MatlabNode> buildChildren(MatlabNode identifier, MatlabNode expression) {
    List<MatlabNode> forExp = Arrays.asList(identifier, TempNodeFactory.newAssignment(), expression);
    
    return Arrays.asList(
    	MatlabNodeFactory.newReservedWord(ReservedWord.For),
    	TempNodeFactory.newExpression(forExp));
    }
    */

    @Override
    protected MatlabNode copyPrivate() {
	return new SimpleForSt(getData(), Collections.emptyList());
    }

    @Override
    public ReservedWord getReservedWord() {
	return ReservedWord.For;
    }

}
