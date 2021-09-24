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

package org.specs.matlabtocl.v2.types.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.CIR.Types.Views.Code.Code;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public final class CLBridgeType extends AVariableType {
    public static final CLBridgeType CL_CHAR = new CLBridgeType("cl_char", "csc");
    public static final CLBridgeType CL_UCHAR = new CLBridgeType("cl_uchar", "cuc");
    public static final CLBridgeType CL_SHORT = new CLBridgeType("cl_short", "css");
    public static final CLBridgeType CL_USHORT = new CLBridgeType("cl_ushort", "cus");
    public static final CLBridgeType CL_INT = new CLBridgeType("cl_int", "csi");
    public static final CLBridgeType CL_UINT = new CLBridgeType("cl_uint", "cui");
    public static final CLBridgeType CL_LONG = new CLBridgeType("cl_long", "csl");
    public static final CLBridgeType CL_ULONG = new CLBridgeType("cl_ulong", "cul");
    public static final CLBridgeType CL_FLOAT = new CLBridgeType("cl_float", "cf");
    public static final CLBridgeType CL_DOUBLE = new CLBridgeType("cl_double", "cd");

    private final String typeCode;
    private final String smallId;

    private CLBridgeType(String typeCode, String smallId) {
        this.typeCode = typeCode;
        this.smallId = smallId;
    }

    @Override
    public Code code() {
        return new ACode(this) {
            @Override
            public String getSimpleType() {
                return CLBridgeType.this.typeCode;
            }

            @Override
            public Set<String> getIncludes() {
                HashSet<String> includes = new HashSet<>();
                includes.add("matisse-cl.h");
                return includes;
            }
        };
    }

    @Override
    public String getSmallId() {
        return this.smallId;
    }

    private static Iterable<CLBridgeType> values() {
        return Arrays.asList(CLBridgeType.CL_CHAR, CLBridgeType.CL_UCHAR,
                CLBridgeType.CL_SHORT, CLBridgeType.CL_USHORT,
                CLBridgeType.CL_INT, CLBridgeType.CL_UINT,
                CLBridgeType.CL_LONG, CLBridgeType.CL_ULONG,
                CLBridgeType.CL_FLOAT, CLBridgeType.CL_DOUBLE);
    }

    public static CLBridgeType getBridgeTypeFor(CLNativeType nativeType) {
        for (CLBridgeType type : values()) {
            if (type.typeCode.equals("cl_" + nativeType.code().getSimpleType())) {
                return type;
            }
        }

        throw new UnsupportedOperationException("Type " + nativeType.code().getSimpleType());
    }

    @Override
    public boolean strictEquals(VariableType type) {
        return type instanceof CLBridgeType && equals(type);
    }
}
