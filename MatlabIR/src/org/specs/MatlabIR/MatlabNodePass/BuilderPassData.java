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

package org.specs.MatlabIR.MatlabNodePass;

import java.util.Collection;
import java.util.Optional;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.storedefinition.StoreDefinition;

import com.google.common.base.Preconditions;

public abstract class BuilderPassData implements DataStore {

    private final DataStore setupBuilder;

    public BuilderPassData(DataStore setupBuilder) {
        this.setupBuilder = setupBuilder;
    }

    @Override
    public Object get(String id) {
        return setupBuilder.get(id);
    }

    @Override
    public String getName() {
        return setupBuilder.getName();
    }

    @Override
    public <T> T get(DataKey<T> key) {
        T value = setupBuilder.get(key);

        Preconditions.checkNotNull(setupBuilder.hasValue(key), "No value present for key '" + key + "'");

        return value;
    }

    @Override
    // public <T, E extends T> Optional<T> set(DataKey<T> key, E value) {
    public <T, E extends T> BuilderPassData set(DataKey<T> key, E value) {
        // return setupBuilder.set(key, value);
        setupBuilder.set(key, value);
        return this;
    }

    // @Override
    // public BuilderPassData set(DataStore setup) {
    // setupBuilder.set(setup);
    //
    // return this;
    // }

    @Override
    public Optional<Object> setRaw(String key, Object value) {
        return setupBuilder.setRaw(key, value);
    }

    protected DataStore getSetupBuilder() {
        return setupBuilder;
    }

    @Override
    public <T> boolean hasValue(DataKey<T> key) {
        return setupBuilder.hasValue(key);
    }

    @Override
    public String toString() {
        return setupBuilder.toString();
        // StringBuilder builder = new StringBuilder();
        //
        // for (DataKey<?> key : getKeys()) {
        // builder.append(key.getName()).append(": ").append(get(key)).append("\n");
        // }
        //
        // return builder.toString();
    }

    // @Override
    // public Map<String, Object> getValuesMap() {
    // return setupBuilder.getValuesMap();
    // }

    @Override
    public void setStrict(boolean value) {
        setupBuilder.setStrict(value);
    }

    @Override
    public <T> Optional<T> remove(DataKey<T> key) {
        return setupBuilder.remove(key);
    }

    @Override
    public Optional<StoreDefinition> getStoreDefinitionTry() {
        return setupBuilder.getStoreDefinitionTry();
    }

    @Override
    public void setStoreDefinition(StoreDefinition definition) {
        setupBuilder.setStoreDefinition(definition);
    }

    @Override
    public Collection<String> getKeysWithValues() {
        return setupBuilder.getKeysWithValues();
    }
}
