/*
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

package pt.up.fe.specs.matisse.weaver.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class VarType extends LinkedHashMap<String, Map<String, String>> {
    /**
     * 
     */
    private static final long serialVersionUID = 791966531344337805L;

    public String put(String function, String var, String type) {
	if (var == null) {
	    throw new RuntimeException("'null' is not allowed as a var name");
	}
	// System.out.println(function+"->"+var+"->"+type);
	if (!containsKey(function)) {
	    put(function, new LinkedHashMap<String, String>());
	}
	Map<String, String> vars = get(function);
	return vars.put(var, type);
    }

    @Override
    public String toString() {
	StringBuilder ret = new StringBuilder();
	for (String function : keySet()) {
	    ret.append("scope ");
	    ret.append(function);
	    ret.append("{\n");
	    Map<String, String> funcVars = get(function);
	    for (String var : funcVars.keySet()) {
		ret.append("\t");
		ret.append(var);
		ret.append(": ");
		ret.append(funcVars.get(var));
		ret.append("\n");
	    }
	    ret.append("}\n\n");
	}
	return ret.toString();
    }

    /**
     * 
     * @param mainFunction
     *            if null, is equivalent to calling toString()
     * @return
     */
    public String toString(String mainFunction) {

	if (mainFunction == null) {
	    return toString();
	}

	StringBuilder ret = new StringBuilder("scope ");
	ret.append(mainFunction);
	ret.append("{\n");
	Map<String, String> main = remove(mainFunction);
	// Check in main is null
	if (main == null) {
	    main = Collections.emptyMap();
	}

	for (String var : main.keySet()) {
	    ret.append("\t");
	    ret.append(var);
	    ret.append(": ");
	    ret.append(main.get(var));
	    ret.append("\n");
	}
	for (String function : keySet()) {
	    ret.append("\tscope ");
	    ret.append(function);
	    ret.append("{\n");
	    Map<String, String> funcVars = get(function);
	    for (String var : funcVars.keySet()) {
		ret.append("\t\t");
		ret.append(var);
		ret.append(": ");
		ret.append(funcVars.get(var));
		ret.append("\n");
	    }
	    ret.append("\t}\n");
	}
	ret.append("}\n\n");
	return ret.toString();
    }
}
