/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.heuristics.schedule;

import org.specs.matlabtocl.v2.heuristics.decisiontree.Node;
import org.specs.matlabtocl.v2.heuristics.decisiontree.RuleChecker;

/**
 * We use ScheduleDecisionTree as the root level instead of the node directly mostly due to generic type erasure. We
 * want to do DataKey&lt;ScheduleDecisionTree&gt;.
 * 
 * @author Luís Reis
 *
 */
public class ScheduleDecisionTree implements Node<ScheduleMethod, SchedulePredictorContext> {

    private Node<ScheduleMethod, SchedulePredictorContext> node;

    public ScheduleDecisionTree(Node<ScheduleMethod, SchedulePredictorContext> node) {
        this.node = node;
    }

    @Override
    public ScheduleMethod decide(SchedulePredictorContext context,
            RuleChecker<SchedulePredictorContext> ruleChecker) {

        return node.decide(context, ruleChecker);
    }
}
