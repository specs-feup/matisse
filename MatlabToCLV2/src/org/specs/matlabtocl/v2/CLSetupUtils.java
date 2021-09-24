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

package org.specs.matlabtocl.v2;

import org.specs.CIR.CirKeys;
import org.specs.matlabtocl.v2.services.DeviceMemoryManagementStrategy;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class CLSetupUtils {
    public static void setDeviceManagementStrategy(DataStore setup, DeviceMemoryManagementStrategy strategy) {
        setup.set(MatisseCLKeys.DEVICE_MEMORY_MANAGEMENT_STRATEGY, strategy);

        switch (strategy) {
        case COPY_BUFFERS:
            setup.remove(CirKeys.CUSTOM_ALLOCATION_HEADER);
            setup.remove(CirKeys.CUSTOM_DATA_ALLOCATOR);
            setup.remove(CirKeys.CUSTOM_FREE_DATA_CODE);
            break;
        case FINE_GRAINED_BUFFERS:
            setup.add(CirKeys.CUSTOM_ALLOCATION_HEADER, "matisse-cl.h");
            setup.add(CirKeys.CUSTOM_FREE_DATA_CODE, "clSVMFree(MATISSE_cl.context, $1)");
            setup.add(CirKeys.CUSTOM_DATA_ALLOCATOR,
                    "clSVMAlloc(MATISSE_cl.context, CL_MEM_READ_WRITE |  CL_MEM_SVM_FINE_GRAIN_BUFFER, $1, 0)");
            break;
        default:
            throw new NotImplementedException(strategy);
        }
    }
}
