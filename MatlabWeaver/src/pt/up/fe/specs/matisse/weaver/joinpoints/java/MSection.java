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

package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ASection;

public class MSection extends ASection {

    private final CommentSt sectionNode;
    private final String label;
    private final String args;

    public MSection(CommentSt section, AMWeaverJoinPoint parent) {
        initMWeaverJP(parent);

        sectionNode = section;

        String comment = section.getCommentString();

        // Extract label and args
        int index = comment.indexOf(" ");
        index = index > -1 ? index : comment.length();
        String label = comment.substring(1, index);

        String args = "";
        if (index < comment.length()) {
            args = comment.substring(index + 1, comment.length());
        }

        this.label = label;
        this.args = args;
    }

    @Override
    public MatlabNode getNode() {
        return sectionNode;
    }

    @Override
    public String getLabelImpl() {
        return label;
    }

    @Override
    public String getArgsImpl() {
        return args;
    }

}
