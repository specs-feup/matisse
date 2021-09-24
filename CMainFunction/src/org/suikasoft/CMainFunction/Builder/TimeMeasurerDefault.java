/**
 * Copyright 2014 SPeCS.
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

package org.suikasoft.CMainFunction.Builder;

import java.util.List;

import org.specs.CIR.Tree.CNode;

/**
 * @author Joao Bispo
 * 
 */
public class TimeMeasurerDefault implements TimeMeasurer {

    private final List<CNode> epilogue;
    private final List<CNode> prologue;

    // private final List<String> literalDeclaration;
    // private final List<String> implementationIncludes;

    public TimeMeasurerDefault(List<CNode> epilogue, List<CNode> prologue) {
	this.epilogue = epilogue;
	this.prologue = prologue;
    }

    /* (non-Javadoc)
     * @see org.suikasoft.CMainFunction.Builder.TimeMeasurer#getPrologue()
     */
    @Override
    public List<CNode> getPrologue() {
	return prologue;
    }

    /* (non-Javadoc)
     * @see org.suikasoft.CMainFunction.Builder.TimeMeasurer#getEpilogue()
     */
    @Override
    public List<CNode> getEpilogue() {
	return epilogue;
    }

    /* (non-Javadoc)
     * @see org.suikasoft.CMainFunction.Builder.TimeMeasurer#getLiteralDeclaration()
     */
    // @Override
    // public List<String> getLiteralDeclaration() {
    // return literalDeclaration;
    // }

    /* (non-Javadoc)
     * @see org.suikasoft.CMainFunction.Builder.TimeMeasurer#getImplementationIncludes()
     */
    /*
    @Override
    public List<String> getImplementationIncludes() {
    return implementationIncludes;
    }
    */

}
