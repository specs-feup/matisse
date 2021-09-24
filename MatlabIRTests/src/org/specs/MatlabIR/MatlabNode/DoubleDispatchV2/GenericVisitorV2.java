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

package org.specs.MatlabIR.MatlabNode.DoubleDispatchV2;

import java.util.Optional;

public class GenericVisitorV2<B, O> extends VisitorWithOutputV2<B, O> {

    private Optional<O> defaultValue = Optional.empty();

    public GenericVisitorV2() {
	super();
    }

    public GenericVisitorV2(VisitorBuilder<B, O> builder) {
	super(builder);
    }

    public void setDefaultValue(O defaultValue) {
	this.defaultValue = Optional.ofNullable(defaultValue);
    }

    @Override
    public O defaultValue(B visited) {
	if (!defaultValue.isPresent()) {
	    return super.defaultValue(visited);
	}

	return defaultValue.get();
    }

}
