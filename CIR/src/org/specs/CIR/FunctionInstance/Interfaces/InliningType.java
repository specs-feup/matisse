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

package org.specs.CIR.FunctionInstance.Interfaces;

/**
 * The types of inlining of a Function Instance. TODO: Still not sure if this class will be needed (as information to be
 * returned from FunctionInstance
 * 
 * @author JoaoBispo
 * 
 *
 */
public enum InliningType {

    /**
     * Inlining is not supported
     */
    NOT_INLINED,
    /**
     * Inlined code can be replaced anywhere in the code (can be used inside expressions and function calls)
     */
    IN_PLACE,
    /**
     * Inlining spans one or more lines, and cannot be used inside instructions (replaces a complete instruction)
     */
    LINES;
}
