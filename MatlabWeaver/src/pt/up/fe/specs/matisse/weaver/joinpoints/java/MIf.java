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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.statements.BlockSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;

import pt.up.fe.specs.matisse.weaver.MatlabJoinpoints;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElse;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElseif;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AIf;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement;

/**
 * @author Tiago Carvalho
 *
 */
public class MIf extends AIf {

    private final IfSt ifSt;
    private final BlockSt block;

    public MIf(IfSt ifToken, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        ifSt = ifToken;
        block = (BlockSt) ifToken.getParent();
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AIf#selectCondition()
     */
    @Override
    public List<? extends AExpression> selectCondition() {
        return Arrays.asList(new MExpression(ifSt.getExpression(), this));
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AIf#selectThen()
     */
    @Override
    public List<? extends ABody> selectThen() {
        return Arrays.asList(new MBody(ifSt.getBodyStatements(), this));

        /*
        List<TomToken> thenTokens = IfStrategies.then(tomToken);
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

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AIf#selectElseif()
     */
    @Override
    public List<? extends AElseif> selectElseif() {
        List<ElseIfSt> elseIfs = ifSt.getParent().getChildrenStream()
                .filter(node -> node instanceof ElseIfSt)
                .map(node -> (ElseIfSt) node)
                .collect(Collectors.toList());

        List<MElseif> elseIfJps = new ArrayList<>();
        for (ElseIfSt elseIf : elseIfs) {
            elseIfJps.add(new MElseif(elseIf, this));
        }

        return elseIfJps;
        // .map(node -> new MElseIf((ElseIfSt) node, this))
        // .collect(Collectors.toList());

        /*
        List<TomToken> elseIfsTokens = IfStrategies.elseif(tomToken);
        List<AElseif> elseifs = new ArrayList<AElseif>();
        if (elseIfsTokens != null)
        for (int i = 0; i < elseIfsTokens.size(); i++) {
        	TomToken elseifToken = elseIfsTokens.get(i);
        	AElseif mElseIf = new MElseif(elseifToken, getFileRoot(), this);
        	elseifs.add(mElseIf);
        }
        return elseifs;
        */
    }

    /* (non-Javadoc)
     * @see org.specs.mweaver.abstracts.joinpoints.AIf#selectElse()
     */
    @Override
    public List<? extends AElse> selectElse() {
        MatlabNode blockSt = ifSt.getParent();
        MatlabNodeIterator iterator = blockSt.getChildrenIterator();

        // Advance until an else is found
        Optional<ElseSt> elseSt = iterator.next(ElseSt.class);

        return elseSt.map(node -> Arrays.asList((AElse) MatlabJoinpoints.newJoinpoint(node, this)))
                .orElseGet(Collections::emptyList);
        /*
        if (!elseSt.isPresent()) {
            return Collections.emptyList();
            // return Arrays.asList(new MBody(Collections.emptyList(), this));
        }
        
        // Collect all statements from else to the end of block
        List<StatementNode> elseSts = new ArrayList<>();
        elseSts.add(elseSt.get());
        while (iterator.hasNext()) {
            elseSts.add((StatementNode) iterator.next());
        }
        
        return Arrays.asList(new MBody(elseSts, this));
        */
    }

    @Override
    public List<? extends AStatement> selectHeader() {
        return Arrays.asList(new MStatement(ifSt, this));
    }

    @Override
    public MatlabNode getNode() {
        return block;
    }

    public IfSt getHeader() {
        return ifSt;
    }
}
