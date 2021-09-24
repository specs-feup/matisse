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

package org.specs.matisselib.matlabinference.functions;

import java.util.List;

import org.specs.matisselib.matlabinference.MatlabFunctionType;
import org.specs.matisselib.matlabinference.MatlabType;

import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public enum MatlabOp implements MatlabFunctionType {

    PLUS {
	@Override
	public List<MatlabType> getTypes(List<MatlabType> inputTypes) {
	    throw new NotImplementedException();
	}
    };

    private final String name;

    private MatlabOp() {
	this.name = SpecsStrings.toLowerCase(name());
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public abstract List<MatlabType> getTypes(List<MatlabType> inputTypes);
}
