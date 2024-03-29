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

package pt.up.fe.specs.matisse.weaver;

import java.util.Arrays;
import java.util.List;

import pt.up.fe.specs.util.collections.SpecsList;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class MatisseLaraApis {

    private static final List<ResourceProvider> MATISSE_LARA_API = SpecsList.newInstance(ResourceProvider.class)
            .concat(ResourceProvider.getResources(LaraCoreApiResource.class))
            .concat(ResourceProvider.getResources(LaraApiResource.class))
            .concat(ResourceProvider.getResources(LaraApiCompatibilityResource.class));
    // Collections.unmodifiableList(SpecsCollections.concatLists(
    // ResourceProvider.getResources(LaraCoreApiResource.class),
    // ResourceProvider.getResources(LaraApiResource.class)));

    private static final List<Class<?>> MATISSE_IMPORTABLE_CLASSES = Arrays.asList(MatlabApiResource.class);

    public static List<ResourceProvider> getApis() {
        return MATISSE_LARA_API;
    }

    public static List<Class<?>> getImportableClasses() {
        return MATISSE_IMPORTABLE_CLASSES;
    }
}
