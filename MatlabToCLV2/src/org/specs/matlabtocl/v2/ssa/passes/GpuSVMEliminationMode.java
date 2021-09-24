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

package org.specs.matlabtocl.v2.ssa.passes;

public enum GpuSVMEliminationMode {
    /**
     * Convert SVM variables to explicit buffers only if all new data transfers are out of the loops where the kernels
     * are invoked.
     */
    ELIMINATE_COPIES_OUT_OF_LOOPS,
    /**
     * Convert SVM variables to explicit buffers only if doing so will not introduce any new data transfers.
     */
    ELIMINATE_NO_ADDED_COPIES,
    /**
     * Do not convert SVM variables to explicit buffers.
     */
    DO_NOT_ELIMINATE
}
