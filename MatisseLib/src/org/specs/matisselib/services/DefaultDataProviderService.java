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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.collections.AccumulatorMap;

public final class DefaultDataProviderService implements DataProviderService {

    @SuppressWarnings("rawtypes")
    private static final DataKey<Map> POST_TYPE_PASS_DATA = KeyFactory.object("post_type_pass_data", Map.class);
    AccumulatorMap<DataService<?>> builds = new AccumulatorMap<>();
    
    private static final boolean ENABLE_LOG = false;

    private final TypedInstance instance;
    private final DataStore dataStore;

    public DefaultDataProviderService(TypedInstance instance, DataStore dataStore) {
        this.instance = instance;
        this.dataStore = dataStore;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> T buildData(DataService<T> dataType) {
        Map map = getMap();

        Object o = map.get(dataType);
        if (o != null) {
            return (T) o;
        }

        builds.add(dataType, 1);
        log("Built " + dataType.getName() + " " + builds.getCount(dataType) + " times");
        T value = dataType.build(instance, dataStore);
        map.put(dataType, value);
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Optional<T> tryGet(DataService<T> dataType) {
        Map map = getMap();

        return Optional.ofNullable((T) map.get(dataType));
    }

    @SuppressWarnings({ "rawtypes" })
    private Map getMap() {
        Map map = dataStore.getTry(POST_TYPE_PASS_DATA).orElseGet(() -> {
            Map newMap = new HashMap<>();
            dataStore.add(POST_TYPE_PASS_DATA, newMap);
            return newMap;
        });

        return map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void update(PostTypeInferencePass pass) {
        Map map = getMap();

        for (Object key : new ArrayList<>(map.keySet())) {
            if (!pass.preserveData((DataService<?>) key)) {

                Object value = map.get(key);
                log("Invalidating " + key + " due to " + pass);
                map.remove(key);
                ((DataService) key).dispose(value);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> void invalidate(DataService<T> dataType) {
        Map map = getMap();

        log("Invalidating " + dataType + " explicitly.");
        Object value = map.get(dataType);
        if (value != null) {
            dataType.dispose((T) value);
            map.remove(dataType);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void close() {
        Map map = getMap();

        for (Object key : map.keySet()) {
            Object value = map.get(key);
            ((DataService) key).dispose(value);
        }
    }

    private static void log(String message) {
    	if (ENABLE_LOG) {
    		System.err.println(message);
    	}
    }
}
