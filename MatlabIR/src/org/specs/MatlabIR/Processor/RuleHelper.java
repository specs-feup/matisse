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

package org.specs.MatlabIR.Processor;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
// public class RuleHelper implements TreeTransformRule {
public class RuleHelper implements StatementRule {

    private final StatementRule rule;

    // public RuleHelper(TreeTransformRule rule) {
    public RuleHelper(StatementRule rule) {
	this.rule = rule;
    }

    public void process(MatlabNode token) {
	// if (!check(token)) {
	// return;
	// }

	try {
	    apply(token);
	} catch (TreeTransformException e) {
	    SpecsLogs.msgInfo("Could not apply MATLAB tree transformation:" + e.getMessage());
	}
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#check(org.specs.MatlabIR.MatlabToken)
     */
    // public boolean check(MatlabToken token) {
    // return rule.check(token);
    // }

    /* (non-Javadoc)
     * @see org.specs.MatlabIR.Processor.TreeTransformRule#apply(org.specs.MatlabIR.MatlabToken)
     */
    // public void apply(MatlabToken token) throws TreeTransformException {
    @Override
    public boolean apply(MatlabNode token) throws TreeTransformException {
	return rule.apply(token);
    }
}
