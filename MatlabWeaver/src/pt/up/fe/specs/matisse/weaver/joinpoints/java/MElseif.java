/*
 * Copyright 2013 SPeCS.
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
package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElseif;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;

public class MElseif extends AElseif {
    private final ElseIfSt elseIf;

    public MElseif(ElseIfSt elseifToken, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        elseIf = elseifToken;
    }

    @Override
    public List<? extends AExpression> selectCondition() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public List<? extends ABody> selectBody() {

        return Arrays.asList(new MBody(elseIf.getBodyStatements(), this));
        /*
        	List<TomToken> thenTokens = IfStrategies.elseifBody(tomToken);
        	List<ABody> thens = new ArrayList<ABody>();
        	if (thenTokens != null)
        	    for (int i = 0; i < thenTokens.size(); i++) {
        		TomToken thenToken = thenTokens.get(i);
        		ABody mBody = new MBody(thenToken, getFileRoot(), this);
        		thens.add(mBody);
        	    }
        	return thens;
        	*/
    }

    @Override
    public MatlabNode getNode() {
        return elseIf;
    }

    public ElseIfSt getHeader() {
        return elseIf;
    }
}
