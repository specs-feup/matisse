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

package org.specs.CIR.Language.Operators;

/**
 * @author Joao Bispo
 * 
 */
public class COperatorData {

    public final COperator op;
    // TODO: Do I really need this option?
    public final boolean invertArgs;

    public COperatorData(COperator op, boolean invertArgs) {
	this.op = op;
	this.invertArgs = invertArgs;
    }

}
