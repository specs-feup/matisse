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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * A Case Declaration statement
 * 
 * @author JoaoBispo
 *
 */
public class CaseSt extends ReservedWordSt {

    CaseSt(StatementData data, Collection<MatlabNode> children) {
        super(data, children);
    }

    CaseSt(int lineNumber, List<MatlabNode> children) {
        super(new StatementData(lineNumber, true), children);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new CaseSt(getData(), Collections.emptyList());
    }

    @Override
    public ReservedWord getReservedWord() {
        return ReservedWord.Case;
    }

    public MatlabNode getExpression() {
        return getChild(1);
    }

    @Override
    public String getStatementCode() {
        return ReservedWord.Case.getLiteral() + " " + getExpression().getCode();
    }

}
