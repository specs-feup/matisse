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

package org.specs.MatlabToCTester.Test;

import java.util.List;
import java.util.Optional;

public interface ArrayReader {

    Optional<ArrayResult> getVariable(String variableName);

    default Optional<Double> getLowThreshold(String variableName) {
	Optional<ArrayResult> array = getVariable(variableName + getLowThresholdSuffix());
	if (!array.isPresent()) {
	    return Optional.empty();
	}

	return Optional.of(array.get().getDouble(0));
    }

    default String getLowThresholdSuffix() {
	return "_low_threshold";
    }

    List<String> getVariableNames();
}
