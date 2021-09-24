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
 * A ParFor statement.
 * <p>
 * *
 * <p>
 * - The first child is the reserved word 'parfor' <br>
 * - The second child is the for expression which should have the following format: <br>
 * -- The first child is the identifier <br>
 * -- The second child is the assignment token <br>
 * -- The third child is an expression <br>
 * 
 * @author JoaoBispo
 *
 */
public class ParForSt extends ForSt {

    ParForSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    ParForSt(int lineNumber, List<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    ParForSt(int lineNumber, MatlabNode identifier, MatlabNode expression) {
	// this(lineNumber, buildChildren(identifier, expression));
	this(lineNumber, Arrays.asList(identifier, expression));

    }

    /*
    static ParForSt newInstance(int lineNumber, List<MatlabNode> children) {
    IdentifierNode identifier = children.get(1).getChild(IdentifierNode.class, 0);
    MatlabNode expression = children.get(1).getChild(IdentifierNode.class, 2);
    
    return new ParForSt(lineNumber, identifier, expression);
    }
    */
    /*
        private static List<MatlabNode> buildChildren(IdentifierNode identifier, MatlabNode expression) {
    	List<MatlabNode> forExp = Arrays.asList(identifier, TempNodeFactory.newAssignment(), expression);
    
    	return Arrays.asList(
    		MatlabNodeFactory.newReservedWord(ReservedWord.Parfor),
    		TempNodeFactory.newExpression(forExp));
        }
    */
    @Override
    protected MatlabNode copyPrivate() {
	return new ParForSt(getData(), Collections.emptyList());
    }

    @Override
    public ReservedWord getReservedWord() {
	return ReservedWord.Parfor;
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }
}
