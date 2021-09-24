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

package org.specs.CIR.Types.Views.Conversion;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class ConversionUtils {

    /**
     * Returns a CToken that converts the given token into the specified type. If no conversion rule is found between
     * the types, throws an exception.
     * 
     * @param token
     * @return
     */
    public static CNode to(CNode token, VariableType targetType) {
        // Get type of token
        VariableType sourceType = token.getVariableType();

        // Try to convert from CToken type to target type
        CNode convertedToken = sourceType.conversion().to(token, targetType);

        if (convertedToken != null) {
            return convertedToken;
        }

        // Conversion did not succeed. Try to convert using the targetType
        convertedToken = targetType.conversion().toSelf(token);

        if (convertedToken != null) {
            return convertedToken;
        }

        throw new RuntimeException("Could not convert from '" + token.getVariableType() + "' ("
                + token.getVariableType().code().getSimpleType() + ") to '"
                + targetType + "' (" + token.getVariableType().code().getSimpleType() + ").");
        // return convertedToken;

    }

    /**
     * Helper method which throws an exception instead of returning null.
     * 
     * @param targetType
     * @param token
     * @return
     */
    /*
    public static CToken toStrict(CToken token, VariableType targetType) {
    CToken convertedToken = to(token, targetType);
    
    if (convertedToken == null) {
        VariableType sourceType = DiscoveryUtils.getVarType(token);
        throw new RuntimeException("Could not convert from '" + sourceType + "' to '" + targetType + "'.\n"
    	    + "Consider adding a conversion rule to method 'to' is source type, or 'toSelf' in target type.");
    }
    
    return convertedToken;
    }
    */

    /**
     * TODO: Check if this function still makes sense. In most cases, we want to know if a function is convertible, and
     * convert it. Also, check if the comments are still correct.
     * 
     * Returns true if 'givenType' is assignable to 'receivingType'.
     * 
     * <p>
     * For instance, an int is assignable to double, but double is not to int. (deprecated, int should be cast to
     * double)
     * 
     * <p>
     * Matrix types are not assignable to scalar types.
     * 
     * <p>
     * OutputAsPointers are assignable-equivalent to their pointing type.
     * 
     * @param sourceType
     * @param targetType
     * @return true if the source type can be assigned to the target type
     */
    public static boolean isAssignable(VariableType sourceType, VariableType targetType) {
        return sourceType.conversion().isAssignable(targetType);
    }

    /**
     * Try to convert a type to Numeric. If it is not possible to convert the type to Numeric, returns null.
     * 
     * @param oldType
     * @return
     */
    /*
    public static VariableType toScalarType(VariableType sourceType) {
    return sourceType.conversion().toScalarType();
    }
    */

}
