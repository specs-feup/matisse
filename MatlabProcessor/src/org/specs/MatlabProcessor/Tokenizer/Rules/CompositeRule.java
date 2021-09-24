/**
 *  Copyright 2012 SPeCS Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.specs.MatlabProcessor.Tokenizer.Rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabProcessor.Tokenizer.TokenizerRule;
import org.specs.MatlabProcessor.Tokenizer.TokenizerState.TokenizerState;

/**
 * @author Joao Bispo
 * @deprecated
 *
 */
@Deprecated
public class CompositeRule implements TokenizerRule {

    private final List<TokenizerRule> rules;
    
    

    /**
     * @param rules
     */
    public CompositeRule(List<TokenizerRule> rules) {
	this.rules = Collections.unmodifiableList(rules);
    }
    
    public CompositeRule(TokenizerRule... rules) {
	this.rules = Collections.unmodifiableList(Arrays.asList(rules));
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabParser.Tokenizer.Rules.TokenizerRule#isAppliable(org.specs.MatlabParser.Tokenizer.TokenizerState)
     */
    @Override
    public boolean isAppliable(TokenizerState state) {
	for(TokenizerRule rule : rules) {
	    if(rule.isAppliable(state)) {
		return true;
	    }
	}

	return false;
    }

    /* (non-Javadoc)
     * @see org.specs.MatlabParser.Tokenizer.Rules.TokenizerRule#apply(org.specs.MatlabParser.Tokenizer.TokenizerState)
     */
    @Override
    public void apply(TokenizerState state) throws TreeTransformException {
	for(TokenizerRule rule : rules) {
	    if(rule.isAppliable(state)) {
		rule.apply(state);
		return;
	    }
	}
	
	throw new TreeTransformException();
    }

}
