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

package org.specs.CIR.Utilities.Inlining;

import java.util.Arrays;
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

public class InliningData {

    private final Set<FunctionName> allInlinables;
    private final Set<FunctionName> inline;
    private final Map<String, FunctionName> supportedMap;

    private InliningData(Collection<FunctionName> enabledInlinables, Collection<FunctionName> supportedInlinables) {
        inline = Sets.newHashSet(enabledInlinables);
        allInlinables = Collections.unmodifiableSet(Sets.newLinkedHashSet(supportedInlinables));
        supportedMap = buildSupportMap(supportedInlinables);
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

    public static InliningData newInstanceWithChecks(boolean allActive, Class<?>... inlinablesEnums) {
        return newInstanceWithChecks(allActive, Arrays.asList(inlinablesEnums));
    }

    public static InliningData newInstanceWithChecks(boolean allActive, List<Class<?>> inlinablesEnums) {

        Collection<FunctionName> inlinables = Sets.newLinkedHashSet();

        for (Class<?> inlinablesEnum : inlinablesEnums) {
            // Check if enum
            if (!inlinablesEnum.isEnum()) {
                SpecsLogs.warn("Given class '" + inlinablesEnum.getName() + "' is not an enum");
                continue;
            }

            Object[] values = inlinablesEnum.getEnumConstants();

            // Check if empty
            if (values.length == 0) {
                continue;
            }

            // Check if is instance of InlinableId
            if (!FunctionName.class.isInstance(values[0])) {
                SpecsLogs.warn("Given class '" + inlinablesEnum.getName() + "' does not implement interface "
                        + FunctionName.class);
                continue;
            }

            for (Object value : values) {

                boolean newElement = inlinables.add(FunctionName.class.cast(value));
                if (!newElement) {
                    SpecsLogs.warn("Found duplicated inlinable name '" + value + "'");
                }
            }

        }

        return newInstanceWithChecks(allActive, inlinables);

    }

    /**
     * Creates a new InlingData which supports a limited set of InlinableIds.
     * 
     * @param allActive
     * @param supportedInlinables
     * @return
     */
    private static InliningData newInstanceWithChecks(boolean allActive, Collection<FunctionName> supportedInlinables) {
        if (allActive) {
            return new InliningData(supportedInlinables, supportedInlinables);
        }
        List<FunctionName> empty = Collections.emptyList();
        return new InliningData(empty, supportedInlinables);
    }

    public boolean inline(FunctionName id) {
        checkId(id);

        return inline.contains(id);
    }

    public void setInline(FunctionName id) {

        if (!checkId(id)) {
            return;
        }

        inline.add(id);
    }

    public void unsetInline(FunctionName id) {

        if (!checkId(id)) {
            return;
        }

        inline.remove(id);
    }

    /**
     * Check if an id is supported.
     * 
     * @param id
     * @return
     */
    private boolean checkId(FunctionName id) {
        // If null, means no check is done
        if (allInlinables == null) {
            return true;
        }

        if (allInlinables.contains(id)) {
            return true;
        }

        SpecsLogs.warn("Id '" + id + "' is not supported.\nSupported operations: " + allInlinables);
        return false;
    }

    public void setInline(List<FunctionName> inline) {
        for (FunctionName id : inline) {
            setInline(id);
        }

    }

    public Collection<FunctionName> getSupportedFunctions() {
        return allInlinables;
    }

    public Collection<String> getSupportedNames() {
        List<String> names = SpecsFactory.newArrayList();

        for (FunctionName function : getSupportedFunctions()) {
            names.add(function.name());
        }

        return names;
    }

    public FunctionName getId(String value) {
        if (supportedMap == null) {
            return null;
        }

        return supportedMap.get(value.toLowerCase());
    }

    @Override
    public String toString() {
        return "Inlinables: " + inline.toString();
    }

    /**
     * @param listOfStrings
     */
    public void setInlineByName(List<String> idNames) {
        for (String idName : idNames) {
            FunctionName functionName = supportedMap.get(idName.toLowerCase());
            if (functionName == null) {
                SpecsLogs.msgInfo("Inline for function '" + idName + "' not supported");
                continue;
            }

            setInline(functionName);
        }

    }

}
