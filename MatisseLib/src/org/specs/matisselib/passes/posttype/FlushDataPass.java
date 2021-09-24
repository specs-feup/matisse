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

package org.specs.matisselib.passes.posttype;

import org.specs.matisselib.services.DataService;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Debug pass: Used to invalidate the data (e.g. size group information), to track down bugs that are caused by improper
 * validation.
 * 
 * @author Luís Reis
 *
 */
public class FlushDataPass implements PostTypeInferencePass {
    @Override
    public void apply(TypedInstance instance, DataStore passData) {
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return false;
    }
}
