/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR.Language.Types;

/**
 * @author Joao Bispo
 * 
 */
public enum COptionalType {

    SIGNED("signed"),
    UNSIGNED("unsigned"),
    SHORT("short"),
    LONG("long");

    // private final int priority;
    private final String declarationName;

    private COptionalType(String declarationName) {
	this.declarationName = declarationName;
    }

    public String getDeclarationName() {
	return declarationName;
    }
}
