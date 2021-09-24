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

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment;

/**
 * @author Tiago
 *
 */
public class MComment extends AComment {

    private final CommentSt comment;

    public MComment(CommentSt comment, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        this.comment = comment;
    }

    /*
    @Override
    public void insert(String position, String code) {
    Action.insert(comment, position, code);
    }
    */

    @Override
    public String getTextImpl() {
        return comment.getCommentString();
    }

    @Override
    public MatlabNode getNode() {
        return comment;
    }

}
