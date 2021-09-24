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

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;

/**
 * Break.
 * 
 * @author JoaoBispo
 *
 */
public class BreakSt extends ReservedWordSt {

    BreakSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new BreakSt(getData(), Collections.emptyList());
    }

    BreakSt(int line) {
	this(new StatementData(line, false), Arrays.asList(MatlabNodeFactory.newReservedWord(ReservedWord.Break)));
    }

    @Override
    public ReservedWord getReservedWord() {
	return ReservedWord.Break;
    }

}
