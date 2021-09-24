/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.StdInd;

public class StdIntFactory {

    public static StdIntType newInt64() {
	return StdIntType.newInstance(64, false);
    }

    public static StdIntType newInt32() {
	return StdIntType.newInstance(32, false);
    }

    public static StdIntType newInt16() {
	return StdIntType.newInstance(16, false);
    }

    public static StdIntType newInt8() {
	return StdIntType.newInstance(8, false);
    }

    public static StdIntType newUInt64() {
	return StdIntType.newInstance(64, true);
    }

    public static StdIntType newUInt32() {
	return StdIntType.newInstance(32, true);
    }

    public static StdIntType newUInt16() {
	return StdIntType.newInstance(16, true);
    }

    public static StdIntType newUInt8() {
	return StdIntType.newInstance(8, true);
    }

}
