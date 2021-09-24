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

package org.specs.MatlabToC.CodeBuilder.SsaToCRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilderService;
import org.specs.matisselib.ssa.instructions.SsaInstruction;

import com.google.common.base.Preconditions;

public final class SsaToCRuleList {
    public static final SsaToCRuleList EMPTY = new SsaToCRuleList(Collections.emptyList());

    private final List<SsaToCRule> rules;

    public SsaToCRuleList(List<SsaToCRule> rules) {
        Preconditions.checkArgument(rules != null);

        this.rules = new ArrayList<>(rules);
    }

    public SsaToCRuleList(SsaToCRuleList... lists) {
        this.rules = new ArrayList<>();
        for (SsaToCRuleList list : lists) {
            this.rules.addAll(list.rules);
        }
    }

    public int size() {
        return this.rules.size();
    }

    public SsaToCRule get(int index) {
        Preconditions.checkElementIndex(index, size());

        return this.rules.get(index);
    }

    public boolean tryApply(SsaToCBuilderService ssaToCBuilderService, CInstructionList currentBlock,
            SsaInstruction instruction) {
        for (final SsaToCRule rule : this.rules) {
            if (rule.accepts(ssaToCBuilderService, instruction)) {
                rule.apply(ssaToCBuilderService, currentBlock, instruction);
                return true;
            }
        }

        return false;
    }
}
