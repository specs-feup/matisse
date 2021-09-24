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

package org.specs.matisselib.ssa;

import org.specs.matisselib.ssa.instructions.AssignmentInstruction;

/**
 * The input of an assignment instruction
 * 
 * @author Luís Reis
 * @see AssignmentInstruction
 *
 */
public interface Input {
    // No need for any fields.
    // We just don't want any random Object to count as an Input.
    // The only member that should really be implemented is toString.
}
