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

package org.specs.CIR.Utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.Types.FunctionName;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * Library functions that should be avoided.
 * 
 * <p>
 * TODO: This class is practically identical to InliningData, check if it makes sense to extract an abstract class.
 * 
 * @author JoaoBispo
 *
 */
public class AvoidableFunctionsData {

    private final Set<FunctionName> supportedAvoidables;
    private final Set<FunctionName> avoidFunctions;
    private final Map<String, FunctionName> avoidMap;

    private AvoidableFunctionsData(Collection<FunctionName> avoidFunctions, Collection<FunctionName> supportedAvoidables) {
	this.avoidFunctions = Sets.newHashSet(avoidFunctions);
	this.supportedAvoidables = Sets.newLinkedHashSet(supportedAvoidables);
	avoidMap = buildSupportMap(supportedAvoidables);
    }

    private static Map<String, FunctionName> buildSupportMap(Collection<FunctionName> supportedInlinables) {
	if (supportedInlinables == null) {
	    return null;
	}

	Map<String, FunctionName> map = Maps.newHashMap();

	for (FunctionName id : supportedInlinables) {
	    FunctionName previousId = map.put(id.toString().toLowerCase(), id);
	    if (previousId != null) {
		SpecsLogs.warn("Incompatible IDs, '" + previousId.getClass().getSimpleName() + "." + previousId
			+ "' and '" + id.getClass().getSimpleName() + "." + id + "'");
	    }
	}

	return map;
    }

    public static AvoidableFunctionsData newInstanceWithChecks(boolean allActive, Class<?>... avoidableEnums) {

	Collection<FunctionName> avoidable = Sets.newLinkedHashSet();

	for (Class<?> avoidableEnum : avoidableEnums) {
	    // Check if enum
	    if (!avoidableEnum.isEnum()) {
		SpecsLogs.warn("Given class '" + avoidableEnum.getName() + "' is not an enum");
		continue;
	    }

	    Object[] values = avoidableEnum.getEnumConstants();

	    // Check if empty
	    if (values.length == 0) {
		continue;
	    }

	    // Check if is instance of InlinableId
	    if (!FunctionName.class.isInstance(values[0])) {
		SpecsLogs.warn("Given class '" + avoidableEnum.getName() + "' does not implement interface "
			+ FunctionName.class);
		continue;
	    }

	    for (Object value : values) {

		boolean newElement = avoidable.add(FunctionName.class.cast(value));
		if (!newElement) {
		    SpecsLogs.warn("Found duplicated inlinable name '" + value + "'");
		}
	    }

	}

	return newInstanceWithChecks(allActive, avoidable);

    }

    /**
     * Creates a new InlingData which supports a limited set of InlinableIds.
     * 
     * @param allActive
     * @param supportedInlinables
     * @return
     */
    public static AvoidableFunctionsData newInstanceWithChecks(boolean allActive,
	    Collection<FunctionName> supportedInlinables) {
	if (allActive) {
	    return new AvoidableFunctionsData(supportedInlinables, supportedInlinables);
	}
	List<FunctionName> empty = Collections.emptyList();
	return new AvoidableFunctionsData(empty, supportedInlinables);
    }

    public boolean avoid(FunctionName id) {
	checkId(id);

	return avoidFunctions.contains(id);
    }

    public void setAvoid(FunctionName id) {

	if (!checkId(id)) {
	    return;
	}

	avoidFunctions.add(id);
    }

    public void unsetAvoid(FunctionName id) {

	if (!checkId(id)) {
	    return;
	}

	avoidFunctions.remove(id);
    }

    /**
     * Check if an id is supported.
     * 
     * @param id
     * @return
     */
    private boolean checkId(FunctionName id) {
	// If null, means no check is done
	if (supportedAvoidables == null) {
	    return true;
	}

	if (supportedAvoidables.contains(id)) {
	    return true;
	}

	SpecsLogs.warn("Id '" + id + "' is not supported");
	return false;
    }

    public void setAvoid(List<FunctionName> inline) {
	for (FunctionName id : inline) {
	    setAvoid(id);
	}

    }

    public Collection<FunctionName> getSupportedFunctions() {
	return supportedAvoidables;
    }

    public Collection<String> getSupportedNames() {
	List<String> names = SpecsFactory.newArrayList();

	for (FunctionName function : getSupportedFunctions()) {
	    names.add(function.name());
	}

	return names;
    }

    public FunctionName getId(String value) {
	if (avoidMap == null) {
	    return null;
	}

	return avoidMap.get(value.toLowerCase());
    }

    @Override
    public String toString() {
	return "Avoidables: " + avoidFunctions.toString();
    }

    /**
     * @param listOfStrings
     */
    public void setAvoidByName(List<String> idNames) {
	for (String idName : idNames) {
	    FunctionName functionName = avoidMap.get(idName.toLowerCase());
	    if (functionName == null) {
		SpecsLogs.msgInfo("Inline for function '" + idName + "' not supported");
		continue;
	    }

	    setAvoid(functionName);
	}

    }

}
