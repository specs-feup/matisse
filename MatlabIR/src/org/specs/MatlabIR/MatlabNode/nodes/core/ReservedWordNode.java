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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Reserved word in MATLAB, as given by the command 'iskeyword' (see class Base.MatlabLanguage.ReservedKeyword).
 * 
 * <p>
 * The content is a String.
 * 
 * @author JoaoBispo
 *
 */
public class ReservedWordNode extends MatlabNode {

    private final ReservedWord reservedWord;

    ReservedWordNode(ReservedWord reservedWord) {
        // super(reservedWord.getLiteral(), Collections.emptyList());

        this.reservedWord = reservedWord;
    }

    ReservedWordNode(Object content, Collection<MatlabNode> children) {
        // Use OCTAVE because it has a super-set of the keywords.
        this(ReservedWord.getReservedWord((String) content, LanguageMode.OCTAVE));
    }

    @Override
    protected MatlabNode copyPrivate() {
        return new ReservedWordNode(getWord());
    }

    public ReservedWord getWord() {
        return reservedWord;
    }

    @Override
    public String getCode() {
        return reservedWord.getLiteral();
    }
}
