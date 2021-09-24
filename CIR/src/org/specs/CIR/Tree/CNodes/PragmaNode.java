/**
 * Copyright 2016 SPeCS.
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

package org.specs.CIR.Tree.CNodes;

import org.specs.CIR.Tree.CNode;

import com.google.common.base.Preconditions;

public class PragmaNode extends CNode {
    private final String content;

    public PragmaNode(String content) {
        Preconditions.checkArgument(content != null);

        this.content = content;
    }

    @Override
    public String toReadableString() {
        return getCode();
    }

    @Override
    public String getCode() {
        return "#pragma " + content;
    }

    @Override
    protected CNode copyPrivate() {
        return new PragmaNode(content);
    }

}
