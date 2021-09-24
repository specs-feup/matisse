/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.CodeBuilder.MatToMatRules;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

/**
 * Rule to be used when converting MatlabToken objects into CToken objects.
 * 
 * @author Joao Bispo
 * 
 */
public interface MatlabToMatlabRule {

    /**
     * Returns true if the rule should be applied, and false otherwise.
     * 
     * @param statement
     * @return
     */
    boolean check(MatlabNode token,  MatlabToCFunctionData data);

    /**
     * Takes a statement and returns one or more Matlab statements.
     * 
     * @param token
     * @throws TreeTransformException
     */
    List<MatlabNode> apply(MatlabNode token,  MatlabToCFunctionData data) throws TreeTransformException;

}
