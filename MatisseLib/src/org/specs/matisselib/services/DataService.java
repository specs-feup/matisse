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

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public final class DataService<T> {
    private final String name;
    private final BiFunction<TypedInstance, DataStore, T> builder;
    private final Consumer<T> disposer;

    public DataService(String name, BiFunction<TypedInstance, DataStore, T> builder) {
        this(name, builder, value -> {
        });
    }

    public DataService(String name, BiFunction<TypedInstance, DataStore, T> builder, Consumer<T> disposer) {
        this.name = name;
        this.builder = builder;
        this.disposer = disposer;
    }

    public String getName() {
        return name;
    }

    public T build(TypedInstance instance, DataStore dataStore) {
        return builder.apply(instance, dataStore);
    }

    public void dispose(T value) {
        disposer.accept(value);
    }

    @Override
    public String toString() {
        return getName();
    }
}
