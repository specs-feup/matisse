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

package org.specs.CIR.CodeGenerator;

import java.util.Collections;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.suikasoft.MvelPlus.MvelSolver;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class VariableCode {

    /**
     * "void"
     */
    public static final String VOID = "void";
    public static final String UNDEFINED = "#undefined_type";

    public static String getInputsDeclaration(String variableName, VariableType variableType) {
        List<String> emptyList = Collections.emptyList();
        return getInputsDeclaration(variableName, variableType, emptyList);
    }

    /**
     * 
     * 
     * @param variableName
     * @param variableType
     * @param values
     * @return
     */
    public static String getInputsDeclaration(String variableName, VariableType variableType, List<String> values) {
        StringBuilder builder = new StringBuilder();

        // Append type declaration
        builder.append(CodeUtils.getDeclaration(variableType, variableName));

        if (!values.isEmpty()) {

            if (values.size() > 1) {
                SpecsLogs
                        .msgWarn(
                                "Initialization of scalar type has more than one initialization value, using only the first value");
            }

            String value = values.get(0);

            builder.append(" = ");
            builder.append(value);
        }

        return builder.toString();
    }

    /**
     * Tries to evaluate the given string to an integer.
     * 
     * @param index
     * @return
     */
    public static String simplifyIndex(String index) {
        // return index;

        // TODO: Throwing exception
        Integer indexSolution = MvelSolver.evaltoInteger(index);

        if (indexSolution == null) {
            return index;
        }

        return indexSolution.toString();
    }

}
