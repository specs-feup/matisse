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

package org.specs.MatlabIR.MatlabNode.nodes.statements.mclass;

import java.util.Arrays;
import java.util.Collections;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

public class ClassNodeFactory {

    public static PropertiesSt newProperties(int line) {
	return new PropertiesSt(line, Collections.emptyList());
    }

    public static PropertiesSt newProperties(int line, MatlabNode attributes) {
	return new PropertiesSt(line, Arrays.asList(attributes));
    }

    public static MethodsSt newMethods(int line) {
	return new MethodsSt(line, Collections.emptyList());
    }

    public static MethodsSt newMethods(int line, MatlabNode attributes) {
	return new MethodsSt(line, Arrays.asList(attributes));
    }

    public static EventsSt newEvents(int line) {
	return new EventsSt(line, Collections.emptyList());
    }

    public static EventsSt newEvents(int line, MatlabNode attributes) {
	return new EventsSt(line, Arrays.asList(attributes));
    }

    public static EnumerationSt newEnumeration(int line) {
	return new EnumerationSt(line, Collections.emptyList());
    }
}
