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

package org.specs.matlabtocl.v2.structs;

import org.specs.CIR.FunctionInstance.Instances.StructInstance;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;
import org.specs.matlabtocl.v2.types.kernel.SizedMatrixType;

public class SizedMatrixStruct extends StructInstance {

    public SizedMatrixStruct(SizedMatrixType type) {
	super(type.code().getSimpleType(), null, getDeclarationCode(type));
    }

    private static String getDeclarationCode(SizedMatrixType type) {

	StringBuilder code = new StringBuilder();

	code.append("struct ");
	code.append(type.getSmallId());
	code.append("{\n   ");
	code.append(type.getUnderlyingRawMatrixType().code().getDeclaration("data"));
	code.append(";\n");
	if (type.containsNumel()) {
	    code.append("   ");
	    code.append(CLNativeType.SIZE_T.code().getDeclaration("length"));
	    code.append(";\n");
	}
	for (int i = 0; i < type.containedDims(); ++i) {
	    code.append("   ");
	    code.append(CLNativeType.SIZE_T.code().getDeclaration("dim" + (i + 1)));
	    code.append(";\n");
	}
	code.append("};\n");

	return code.toString();

    }

}
