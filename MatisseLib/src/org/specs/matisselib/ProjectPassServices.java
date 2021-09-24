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

package org.specs.matisselib;

import java.util.Collections;

import org.specs.matisselib.services.AdditionalInformationBuildersService;
import org.specs.matisselib.services.DataProviderService;
import org.specs.matisselib.services.GlobalTypeProvider;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.services.TypedInstanceProviderService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

/**
 * Contains keys for services available available before the type inference pass.
 * 
 * @author JoaoBispo
 *
 */
public class ProjectPassServices {

    public static final DataKey<TypedInstanceProviderService> TYPED_INSTANCE_PROVIDER = KeyFactory.object(
            "typed_instance_provider", TypedInstanceProviderService.class);

    public static final DataKey<SystemFunctionProviderService> SYSTEM_FUNCTION_PROVIDER = KeyFactory.object(
            "system_function_provider", SystemFunctionProviderService.class);

    public static final DataKey<ScalarValueInformationBuilderService> SCALAR_VALUE_INFO_BUILDER_PROVIDER = KeyFactory
            .object(
                    "scalar_value_information_builder_provider", ScalarValueInformationBuilderService.class);

    public static final DataKey<AdditionalInformationBuildersService> ADDITIONAL_INFORMATION_BUILDERS = KeyFactory
            .object("additional_Information_builders", AdditionalInformationBuildersService.class)
            .setDefault(() -> new AdditionalInformationBuildersService(Collections.emptyList()));

    public static final DataKey<DataProviderService> DATA_PROVIDER = KeyFactory
            .object("data_provider", DataProviderService.class);

    public static final DataKey<GlobalTypeProvider> GLOBAL_TYPE_PROVIDER = KeyFactory
            .object("global_type_provider", GlobalTypeProvider.class);
}
