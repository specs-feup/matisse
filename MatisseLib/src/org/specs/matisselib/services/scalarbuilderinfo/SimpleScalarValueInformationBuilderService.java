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

package org.specs.matisselib.services.scalarbuilderinfo;

import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.sizeinfo.ScalarValueInformation;
import org.specs.matisselib.helpers.sizeinfo.SimpleScalarValueInformation;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;

public class SimpleScalarValueInformationBuilderService implements ScalarValueInformationBuilderService {

    @Override
    public ScalarValueInformation build(Function<String, Optional<VariableType>> typeGetter) {
	return new SimpleScalarValueInformation(typeGetter);
    }

}
