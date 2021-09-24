/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

public class ParallelUtils {
    public static List<Integer> getNonOneIndices(Function<String, Optional<VariableType>> typeGetter,
            List<String> localSizes) {

        List<Integer> nonOnes = new ArrayList<>();
        for (int i = 0; i < localSizes.size(); i++) {
            String localSize = localSizes.get(i);

            Optional<VariableType> maybeType = typeGetter.apply(localSize);
            if (ScalarUtils.isScalar(maybeType)) {
                ScalarType type = (ScalarType) maybeType.get();

                Number number = type.scalar().getConstant();
                if (number.toString().equals("1") || number.toString().equals("1.0")) {
                    continue;
                }
            }

            nonOnes.add(i);
        }

        return nonOnes;
    }

    public static int getSharedLocalIndex(List<Integer> nonOneIndices) {
        return nonOneIndices.isEmpty() ? 0 : nonOneIndices.get(0);
    }
}
