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

package org.suikasoft.CMainFunction.Builder;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;

/**
 * @author Joao Bispo
 * 
 */
public class KernelTimeFactory {

    private final FunctionInstance printf;

    public KernelTimeFactory(ProviderData data) {
	this.printf = CStdioFunction.PRINTF.newCInstance(data);
    }

    public TimeMeasurer newInstance(MainFunctionTarget kernelTime) {
	return new TimeMeasurerDefault(kernelTime.getEpilogue(printf), kernelTime.getPrologue(printf));
    }
}
