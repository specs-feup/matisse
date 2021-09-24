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

package org.specs.MatlabToC.MatlabRules;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeUtils;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabIR.Processor.TreeTransformRule;

/**
 * VAR = [VAR ; SOMETHING]
 * 
 * -> Ou tem apenas um ROW; -> Ou tem vários ROW, cada ROW com apenas um elemento
 * 
 * -> Primeiro elemento tem que ser o mesmo id que left hand
 * 
 * Transf: matisse_concat_vertical(var, something, etc...)
 * 
 * (not implemented yet)
 * 
 * @author Joao Bispo
 * 
 */
public class ConcatIdiom implements TreeTransformRule {

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#check(org.specs.MatlabIR.MatlabToken)
     */
    // @Override
    private static boolean check(MatlabNode token) {
        // Check if assignment
        if (!(token instanceof AssignmentSt)) {
            return false;
        }

        // if (MatlabNodeUtils.getVariableNames(StatementAccess.getAssignmentLeftHand(token)).get(0).equals("mag")) {
        if (MatlabNodeUtils.getVariableNames(((AssignmentSt) token).getLeftHand()).get(0).equals("mag")) {
            System.out.println("TOKEN:\n" + token);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    @Override
    public boolean apply(MatlabNode token) throws TreeTransformException {
        if (!check(token)) {
            return false;
        }
        // TODO Auto-generated method stub

        return true;
    }

}
