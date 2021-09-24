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

package org.specs.matisselib.services;

import java.util.Optional;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.DataStoreOwned;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.typeinference.PostTypeInferencePass;

/**
 * @see DefaultDataProviderService
 * @see ProjectPassServices#DATA_PROVIDER
 * @author Luís Reis
 *
 */
public interface DataProviderService extends DataStoreOwned {
    /**
     * @see CompilerDataProviders
     */
    <T> T buildData(DataService<T> dataType);

    <T> Optional<T> tryGet(DataService<T> dataType);

    <T> void invalidate(DataService<T> dataType);

    void update(PostTypeInferencePass pass);

    void close();
}
