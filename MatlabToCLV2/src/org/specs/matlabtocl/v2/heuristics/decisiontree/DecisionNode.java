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

package org.specs.matlabtocl.v2.heuristics.decisiontree;

public class DecisionNode<T, C> implements Node<T, C> {
    private final String rule;
    private final double minimum;
    private final Node<T, C> yes;
    private final Node<T, C> no;

    public DecisionNode(String rule, double minimum, Node<T, C> yes, Node<T, C> no) {
        this.rule = rule;
        this.minimum = minimum;
        this.yes = yes;
        this.no = no;
    }

    @Override
    public T decide(C context, RuleChecker<C> ruleChecker) {
        if (ruleChecker.meetsThreshold(context, rule, minimum)) {
            return yes.decide(context, ruleChecker);
        } else {
            return no.decide(context, ruleChecker);
        }
    }
}
