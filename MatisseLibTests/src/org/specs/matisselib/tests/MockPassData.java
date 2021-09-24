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

package org.specs.matisselib.tests;

import org.suikasoft.jOptions.DataStore.ADataStore;
import org.suikasoft.jOptions.Datakey.DataKey;

public class MockPassData extends ADataStore {

    public MockPassData() {
        super("MockDataStore");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(DataKey<T> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    // public <T, E extends T> Optional<T> set(DataKey<T> key, E value) {
    public <T, E extends T> MockPassData set(DataKey<T> key, E value) {
        throw new UnsupportedOperationException();
    }

    // @Override
    // public MockPassData set(DataStore setup) {
    // throw new UnsupportedOperationException();
    // }

}
