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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Node representing a Matlab Function file.
 * 
 * <p>
 * It can have one or more 'Function' children.
 * 
 * @author JoaoBispo
 *
 */
public class FunctionFileNode extends FileNode {

    FunctionFileNode(Collection<? extends MatlabNode> children, FileNodeContent content) {
        super(content, children);

    }

    @Override
    protected MatlabNode copyPrivate() {
        return new FunctionFileNode(Collections.emptyList(), getFileContent());
    }

    @Override
    protected Map<String, MatlabUnitNode> buildUnitsMap() {
        Map<String, MatlabUnitNode> map = new HashMap<>();

        // For each FunctionNode, map the name to the node
        getChildren(FunctionNode.class).stream()
                .forEach(node -> map.put(node.getFunctionName(), node));

        return map;
    }

    @Override
    public FunctionNode getMainFunction() {
        return getChild(FunctionNode.class, 0);
    }

    public String getMainFunctionName() {
        return getMainFunction().getFunctionName();
    }

    public List<FunctionNode> getSubFunctions() {
        return getChildren(FunctionNode.class).subList(1, getNumChildren());
    }

    @Override
    public String getMainUnitName() {
        return getMainFunctionName();
    }
}
