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

package org.specs.CIR.Language;


/**
 * @author Joao Bispo
 *
 */
public enum StdintType {

    Int8("int8_t"),
    Int16("int16_t"),
    Int32("int32_t"),
    Int64("int64_t"),
    Uint8("uint8_t"),
    Uint16("uint16_t"),
    Uint32("uint32_t"),
    Uint64("uint64_t");
    
    private final String stdintName;

    
    /**
     * Constructor 
     */
    private StdintType(String stdintName) {
	this.stdintName = stdintName;
    }
    
    /**
     * The stdint declaration name. E.g., int32_t for Int32.
     * @return the stdintName
     */
    public String getDeclaration() {
	return stdintName;
    }
}
