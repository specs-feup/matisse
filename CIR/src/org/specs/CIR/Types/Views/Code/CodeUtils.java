/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIR.Types.Views.Code;

import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Types.VariableType;

public class CodeUtils {

    public static String getSimpleType(VariableType type) {
	return type.code().getSimpleType();
    }

    public static String getDeclaration(VariableType type, String variableName) {
	return type.code().getDeclaration(variableName);
    }

    public static String getDeclarationWithInputs(VariableType type, String name, List<String> values) {
	return type.code().getDeclarationWithInputs(name, values);
    }

    public static String getType(VariableType type) {
	return type.code().getType();
    }

    public static String getReturnType(VariableType type) {
	return type.code().getReturnType();
    }

    public static Set<FunctionInstance> getInstances(VariableType type) {
	return type.code().getInstances();
    }

    public static Set<String> getIncludes(VariableType type) {
	return type.code().getIncludes();
    }

}
