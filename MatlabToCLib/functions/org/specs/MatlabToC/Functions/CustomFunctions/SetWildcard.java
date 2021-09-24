/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.CustomFunctions;

import java.util.Collection;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabTemplate;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class SetWildcard {

    /**
     * @param arrayType
     * @param indexesWithColon
     * @param totalIndexes
     * @param functionSettings
     * @return
     */
    public static FunctionInstance newSetWildcard(Collection<Integer> indexesWithColon, int totalIndexes,
            ProviderData data) {

        // Add M file to the engine
        String functionName = addSetWildcard(indexesWithColon, totalIndexes,
                MFileProvider.getEngine());

        // Get instance
        return MFileProvider.getInstance(functionName, data);
    }

    /**
     * Function call receives: - A matrix, which will be accessed; - A number of indexes, either scalar or matrices, in
     * number equal to totalIndexes
     * 
     * @param indexesWithColon
     * @param totalIndexes
     * @param engine
     * @return the name of the created function
     */
    public static String addSetWildcard(Collection<Integer> indexesWithColon, int totalIndexes,
            MatlabToCEngine engine) {

        String functionName = getFunctionName(indexesWithColon, totalIndexes);
        String mCode = getMatlabCode(indexesWithColon, totalIndexes, engine);

        engine.addWithCheck(functionName, mCode);

        return functionName;
    }

    /**
     * @param indexesWithColon
     * @param totalIndexes
     * @param engine
     * @return
     */
    private static String getMatlabCode(Collection<Integer> indexesWithColon, int totalIndexes,
            MatlabToCEngine engine) {
        StringBuilder builder = null;

        String code = SpecsIo.getResource(BuiltinTemplate.SET_WILDCARD);

        // Function name
        code = code.replace("<FUNCTION_NAME>", getFunctionName(indexesWithColon, totalIndexes));

        // Non-wildcard indexes
        String indexPrefix = "index";
        builder = new StringBuilder();
        for (int i = 0; i < totalIndexes; i++) {

            // Ignore if wildcard
            if (indexesWithColon.contains(i)) {
                continue;
            }

            builder.append(", ").append(indexPrefix).append(i + 1);

        }

        code = code.replace("<NON_WILDCARD_INDEXES>", builder.toString());

        // Last Index - If last index is a wildcard, use template. Otherwise, simple assign
        String lastIndexCode = "";
        if (indexesWithColon.contains(totalIndexes - 1)) {
            lastIndexCode = SpecsIo.getResource(BuiltinTemplate.WILDCARD_LAST);

            lastIndexCode = lastIndexCode.replace("<TOTAL_INDEXES>", Integer.toString(totalIndexes));
        }
        /*
        else {
            lastIndexCode = "lastIndex = size(X, " + totalIndexes + ");";
        }
        */

        code = code.replace("<LAST_INDEX>", lastIndexCode);

        // Access Matrix - e.g., access_3index(X, 1:size(X,1), index2, 1:lastIndex)
        builder = new StringBuilder();

        // Add function
        MatlabTemplate accessTemplate = new AccessMatrixIndexes(totalIndexes);
        engine.forceLoad(accessTemplate);
        String functionName = accessTemplate.getName();
        builder.append(functionName).append("(X");

        // Indexes but the last
        for (int i = 0; i < totalIndexes; i++) {
            builder.append(", ");
            if (indexesWithColon.contains(i)) {
                // If last index
                if (i == totalIndexes - 1) {
                    // last index
                    builder.append("1:lastIndex");

                } else {
                    builder.append("1:size(X,");
                    builder.append(i + 1);
                    builder.append(")");
                }

            } else {
                builder.append(indexPrefix).append(i + 1);
            }
        }
        builder.append(")");

        code = code.replace("<INDEXES_CALL>", builder.toString());

        return code;
        // return builder.toString();
    }

    /**
     * @param indexesWithColon
     * @param totalIndexes
     * @return
     */
    private static String getFunctionName(Collection<Integer> indexesWithColon, int totalIndexes) {
        StringBuilder builder = new StringBuilder();

        String prefix = "set";

        // set_w1_w3_3
        builder.append(prefix);
        for (Integer index : indexesWithColon) {
            builder.append("_w").append(index + 1);
        }

        builder.append("_").append(totalIndexes);

        return builder.toString();
    }

}
