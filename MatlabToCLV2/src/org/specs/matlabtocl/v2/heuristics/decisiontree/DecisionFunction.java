package org.specs.matlabtocl.v2.heuristics.decisiontree;

import java.io.Serializable;

public interface DecisionFunction<C, T> extends Serializable {
    public T apply(C context, RuleChecker<C> ruleChecker);
}
