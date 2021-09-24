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

package org.specs.CIR.Types.ATypes.Scalar;

import java.util.Map;
import java.util.Optional;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class FittingRules {

    private final Map<Class<? extends ScalarType>, FittingRule> storeRules;
    private final Map<Class<? extends ScalarType>, FittingRule> fitIntoRules;

    public FittingRules(Map<Class<? extends ScalarType>, FittingRule> storeRules,
	    Map<Class<? extends ScalarType>, FittingRule> fitsIntoRules) {

	this.storeRules = SpecsFactory.assignMap(storeRules);
	this.fitIntoRules = SpecsFactory.assignMap(fitsIntoRules);
    }

    /**
     * @param sourceType
     * @param selfType
     * @return true if current type stores source type
     */
    public Optional<Boolean> stores(ScalarType sourceType, ScalarType selfType) {

	FittingRule fitRule = getRule(storeRules, sourceType);
	System.out.println("RUle:" + fitRule);

	if (fitRule == null) {
	    return Optional.empty();
	}

	return Optional.of(fitRule.fitsInto(sourceType, selfType));
    }

    /**
     * 
     * @param selfType
     * @param targetType
     * @return true if current type fits into target type
     */
    public Optional<Boolean> fitsInto(ScalarType selfType, ScalarType targetType) {
	FittingRule fitRule = getRule(fitIntoRules, targetType);

	// FittingRule fitRule = fitIntoRules.get(targetType.getClass());
	if (fitRule == null) {
	    return Optional.empty();
	}

	return Optional.of(fitRule.fitsInto(selfType, targetType));
    }

    private static FittingRule getRule(Map<Class<? extends ScalarType>, FittingRule> rules, ScalarType keyType) {
	// Iterate over the keys, check if any of them implements the given keyType
	for (Class<? extends ScalarType> aClass : rules.keySet()) {
	    if (aClass.isInstance(keyType)) {
		return rules.get(aClass);
	    }
	}

	return null;
    }

    @Override
    public String toString() {
	return "Fits Into Rules:\n" + fitIntoRules + "\nStores rules:\n" + storeRules;
    }

}
