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

import org.specs.matisselib.services.LogService;
import org.specs.matisselib.services.NamingService;
import org.specs.matisselib.services.WideScopeService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

/**
 * Contains keys for services available available before the type inference pass.
 * 
 * @author JoaoBispo
 *
 */
public class PreTypeInferenceServices {

    public static final DataKey<WideScopeService> WIDE_SCOPE = KeyFactory.object("wide_scope", WideScopeService.class);

    public static final DataKey<NamingService> COMMON_NAMING = KeyFactory.object("common_naming", NamingService.class);

    public static final DataKey<LogService> LOG = KeyFactory.object("log", LogService.class);
}
