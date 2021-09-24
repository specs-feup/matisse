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

package org.specs.matisselib.passmanager.data;

import java.util.Optional;

import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Extends PassData to include keys mapped to a specific {@code FunctionIdentification}.
 * 
 * @author JoaoBispo
 *
 */
public interface PassManagerData extends DataStore {

    /**
     * Sets the {@code FunctionIdentification} to be used as a key.
     * 
     * <p>
     * TODO: Deprecated this method, use getData instead
     * 
     * @param functionId
     * @return the value previously set
     */
    Optional<FunctionIdentification> setFunctionId(Optional<FunctionIdentification> functionId);

    boolean hasFunctionId();

    // Returns a new PassManager data for the given functionId
    Optional<PassManagerData> newData(Optional<FunctionIdentification> functionId);

}
