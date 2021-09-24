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

package org.specs.CIR.Types.Views.Conversion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;

/**
 * @author Joao Bispo
 * 
 */
public class ConversionRules {

    /**
     * Especialization of an HashMap.
     * 
     * @author Joao Bispo
     * 
     */
    public static class ConversionMap extends LinkedHashMap<Class<? extends VariableType>, ConversionRule> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    }

    // private final Map<Class<? extends VariableType>, ConversionRule> toRules;
    private final ConversionMap toRules;
    // private final Map<Class<? extends VariableType>, ConversionRule> toSelfRules;
    private final ConversionMap toSelfRules;

    public ConversionRules(ConversionMap toRules, ConversionMap toSelfRules) {
	// Map<Class<? extends VariableType>, ConversionRule> toSelfRules) {

	this.toRules = assignMap(toRules);
	this.toSelfRules = assignMap(toSelfRules);
    }

    private static ConversionMap assignMap(ConversionMap map) {

	if (map == null) {
	    return new ConversionMap();
	}

	return map;
    }

    public ConversionMap getToRules() {
	return toRules;
    }

    public boolean isConvertibleTo(VariableType type) {
	return toRules.containsKey(type.getClass());
    }

    public boolean isConvertibleToSelf(VariableType type) {
	return toSelfRules.containsKey(type.getClass());
    }

    public CNode convertTo(CNode token, VariableType targetType) {
	return convertPrivate(token, targetType, toRules);
    }

    public CNode convertToSelf(CNode token, VariableType selfType) {
	return convertPrivate(token, selfType, toSelfRules);
    }

    private static CNode convertPrivate(CNode token, VariableType targetType,
	    Map<Class<? extends VariableType>, ConversionRule> rules) {

	ConversionRule rule = rules.get(targetType.getClass());
	// if (rule == null) {
	if (rule != null) {
	    return rule.convert(token, targetType);
	}

	// Could not find a rule for the specific type, iterate over the supported rules to find a supported instance
	for (Class<? extends VariableType> aClass : rules.keySet()) {
	    if (aClass.isInstance(targetType)) {
		rule = rules.get(aClass);
		return rule.convert(token, targetType);
	    }
	}

	// Could not convert
	return null;
	// LoggingUtils.msgWarn("Could not convert from '" + DiscoveryUtils.getVarType(token) + "' to '" + targetType
	// + "'");
	// return token;

	// return rule.convert(token, targetType);
    }

}
