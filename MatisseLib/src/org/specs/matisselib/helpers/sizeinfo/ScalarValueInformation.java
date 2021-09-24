/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.helpers.sizeinfo;

import java.io.Closeable;
import java.util.HashSet;
import java.util.List;

import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;

public abstract class ScalarValueInformation implements Closeable {
    public abstract ScalarValueInformation copy();

    public abstract void buildScalarCopy(String outScalar, String inScalar);

    public abstract void addAlias(String oldValue, String newValue);

    public abstract boolean areSameValue(String v1, String v2);

    public abstract boolean isKnownLessThan(String v1, String v2);

    public boolean isKnownGreaterThan(String v1, String v2) {
        return isKnownLessThan(v2, v1);
    }

    public abstract boolean isKnownLessOrEqualTo(String v1, String v2);

    public void setAtLeast(String numel, String minimum, String context) {
    }

    public abstract void setAtLeast(String numel, String minimum);

    public abstract void setUpTo(String value, String maximum);

    public void setUpTo(String value, String maximum, String context) {
    }

    public void setTrue(String value) {
    }

    public abstract void specifyConstant(String var, double value);

    public void addScalarFunctionCallInformation(FunctionCallInstruction functionCall) {
        addScalarFunctionCallInformation(functionCall, null);
    }

    public void addScalarFunctionCallInformation(FunctionCallInstruction functionCall, String context) {
        // By default, do nothing
    }

    @Override
    public abstract void close();

    public void setRangeSize(String size, String start, String end) {
        // By default, do nothing
    }

    public abstract boolean isKnownEqual(String var, int i);

    public abstract boolean isKnownNotEqual(String var, int i);

    public abstract boolean isKnownPositive(String var);

    public abstract boolean isKnownNegative(String var);

    public boolean growsWith(List<String> values, List<String> reference, String ctx1, String ctx2) {
        for (String value : values) {
            if (!reference.contains(value)) {
                return false;
            }
        }

        return true;
    }

    public boolean mayCollide(List<String> values1, List<String> values2, List<String> reference, String ctx1,
            String ctx2) {

        return !new HashSet<>(values1).equals(new HashSet<>(reference))
                || new HashSet<>(values2).equals(new HashSet<>(reference));
    }
}
