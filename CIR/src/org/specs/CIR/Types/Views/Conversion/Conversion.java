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

package org.specs.CIR.Types.Views.Conversion;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;

/**
 * Object that converts between VariableTypes.
 * 
 * @author Joao Bispo
 * 
 */
public interface Conversion {

    /**
     * 
     * @param type
     * @return true if there are rules to convert the current type to the given type
     */
    boolean isConvertibleTo(VariableType type);

    /**
     * Returns a CToken that converts the given token, of the current type, into the specified type. If no conversion
     * rule is found between the types, returns null.
     * 
     * @param token
     * @return
     */
    CNode to(CNode token, VariableType type);

    /**
     * 
     * @param type
     * @return true if there are rules to convert from the given type to the current type
     */
    boolean isConvertibleToSelf(VariableType type);

    /**
     * Returns a CToken that converts the given token into the current type. If no conversion rule is found between the
     * types, returns null.
     * 
     * @param token
     * @return
     */
    CNode toSelf(CNode token);

    /**
     * Try to extract a scalar type from current type. If it is not possible, returns null.
     * 
     * <p>
     * Should return a base type (e.g., remove pointer information) and maintain information about constants.
     * 
     * @return
     */
    // VariableType toScalarType();

    /**
     * 
     * <p>
     * ATTENTION: This method exists for compatibility reason with older CIR code. There are plans to removed this
     * method in the future.
     * 
     * @param targetType
     * @return true if the current type can be assigned to the target type without any kind of conversion
     */
    boolean isAssignable(VariableType targetType);

    /**
     * Helper method which throws an exception instead of returning null.
     * 
     * @param token
     * @param type
     * 
     * @return
     */
    // CToken toStrict(CToken token, VariableType type);

    /**
     * Helper method which throws an exception instead of returning null.
     * 
     * @param token
     * @return
     */
    // CToken toSelfStrict(CToken token);

}
