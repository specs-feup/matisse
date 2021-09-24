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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor;

import java.util.List;
import java.util.Map;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.MatlabToCFunctionKeys;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules.GeneralStatementRules;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementRules.SpecialStatementRule;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * @author Joao Bispo
 * 
 */
public class StatementProcessor {

    private final LanguageMode languageMode;
    // Statement rules for special cases to be applied before the general rules.
    private final List<MatlabToCRule> specialRules;
    // General rules for specific statement types
    private final BiFunctionClassMap<MatlabNode, MatlabToCFunctionData, CNode> generalRules;

    public StatementProcessor(LanguageMode languageMode, DataStore setup) {
        this.languageMode = languageMode;
        // Using a linked list, since rules are always accessed sequentially
        specialRules = initSpecialRules(setup);

        // Get default general rules
        generalRules = GeneralStatementRules.getMatlabToCRules();
    }

    /**
     * @param setup
     * @return
     */
    private static List<MatlabToCRule> initSpecialRules(DataStore setup) {
        List<MatlabToCRule> rules = SpecsFactory.newArrayList();

        StringList ruleNameList = setup.get(MatlabToCFunctionKeys.SPECIAL_STATEMENT_RULES);
        Map<String, SpecialStatementRule> translationMap = SpecsEnums.buildMap(SpecialStatementRule.values());

        for (String ruleName : ruleNameList.getStringList()) {
            SpecialStatementRule ruleEnum = translationMap.get(ruleName);

            if (ruleEnum == null) {
                continue;
            }

            rules.add(ruleEnum.getRule());
        }

        return rules;
    }

    /**
     * Adds a statement rule for a special case. This rule will be given priority over the previous rules.
     * 
     * @param statementRule
     */
    public void addSpecialRule(MatlabToCRule rule) {
        specialRules.add(0, rule);
    }

    public CNode process(StatementNode statement, MatlabToCFunctionData data) {
        // Set line number
        StatementData sData = statement.getData();
        data.setLineNumber(sData.getLine());

        // Apply the special rules
        CNode result = applySpecialRules(statement, data);

        // Check if special rules could be applied
        if (result != null) {
            return result;
        }

        // Apply general statement rules
        // StatementType type = StatementUtils.getType(statement);
        return generalRules.apply(statement, data);
        /*
        MatlabToCRule rule = generalRules.get(statement.getClass());
        if (rule == null) {
            String msg = "Rule not defined for statements of type '" + statement.getNodeName()
        	    + "'\n -> Statement code:"
        	    + statement.toString();
        
            throw new RuntimeException(msg);
        }
        
        result = rule.matlabToC(statement, data);
        
        return result;
        */
    }

    /**
     * @param token
     * @param data
     * @return
     */
    private CNode applySpecialRules(MatlabNode token, MatlabToCFunctionData data) throws MatlabToCException {

        for (MatlabToCRule rule : specialRules) {
            CNode result = rule.apply(token, data);
            // CNode result = rule.matlabToC(token, data);

            // If result not null, means that transformation was successfully applied
            if (result != null) {
                return result;
            }
        }

        // No transformation could be applied, return null
        return null;
    }

    public CNode process(String matlabCode, MatlabToCFunctionData data) {

        MatlabParser parser = new MatlabParser(languageMode);
        CInstructionList instructions = new CInstructionList();

        for (StatementNode statement : parser.parse(matlabCode).getStatements()) {
            CNode instruction = process(statement, data);
            instructions.addInstruction(instruction);
        }

        return instructions.toCNode();
    }

    public LanguageMode getLanguageMode() {
        return languageMode;
    }

}
