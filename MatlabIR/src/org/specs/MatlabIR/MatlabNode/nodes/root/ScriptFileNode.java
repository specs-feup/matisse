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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Node representing a Matlab script.
 * 
 * <p>
 * It can have a single 'Script' child.
 * 
 * @author JoaoBispo
 *
 */
public class ScriptFileNode extends FileNode {

    private ScriptFileNode(Collection<? extends MatlabNode> children, FileNodeContent content) {
        super(content, children);

    }

    ScriptFileNode(ScriptNode scriptNode, FileNodeContent content) {
        this(Arrays.asList(scriptNode), content);
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new ScriptFileNode(Collections.emptyList(), getFileContent());
    }

    @Override
    public String getMainUnitName() {
        return SpecsIo.removeExtension(getFilename());
    }
}
