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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.matisselib.matlabinference.utils.SystemFunctionTypes;
import org.specs.matisselib.services.UserFileProviderService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.providers.StringProvider;

/**
 * Populates a PassData with the bootstrap services needed for the next steps.
 * 
 * @author JoaoBispo
 *
 */
public class MatisseInit implements KeyUser {

    /**
     * Access to available M-Files in the project.
     */
    public static final DataKey<UserFileProviderService> MFILES_SERVICE = KeyFactory
            .object("mfiles_service", UserFileProviderService.class);

    /**
     * Built-in MATLAB function types.
     */
    public static final DataKey<SystemFunctionTypes> SYSTEM_FUNCTION_TYPES = KeyFactory
            .object("system_function_types", SystemFunctionTypes.class);

    /**
     * Print stream to be used (default is System.err)
     */
    public static final DataKey<PrintStream> PRINT_STREAM = KeyFactory
            .object("print_stream", PrintStream.class).setDefault(() -> System.err);

    /**
     * Print stream to be used (default is System.err)
     */
    public static final DataKey<StringBuilder> PASS_LOG = KeyFactory
            .object("pass_log", StringBuilder.class);

    /**
     * Reporter which accepts Matlab nodes.
     */
    // public static final DataKey<NodeReportService> NODE_REPORT_SERVICE = KeyFactory
    // .object("node_report_service", NodeReportService.class);

    @Override
    public Collection<DataKey<?>> getWriteKeys() {
        return Arrays.asList(MatisseInit.MFILES_SERVICE, MatisseInit.PRINT_STREAM, MatisseInit.PASS_LOG);
    }

    public DataStore newPassData(Map<String, StringProvider> availableFiles,
            DataView additionalServices) {

        return newPassData(LanguageMode.MATLAB, availableFiles, additionalServices);
    }

    public DataStore newPassData(LanguageMode languageMode, Map<String, StringProvider> availableFiles,
            DataView additionalServices) {
        DataStore passData = new CommonPassData("pass-data");

        passData.addAll(additionalServices);

        // Create MFilesService
        passData.add(MatisseInit.MFILES_SERVICE, new UserFileProviderService(languageMode, availableFiles));

        // Create SystemFunctions
        passData.add(MatisseInit.SYSTEM_FUNCTION_TYPES, SystemFunctionTypes.newInstance());

        // Add printstream
        passData.add(MatisseInit.PRINT_STREAM, System.err);

        // Add passlog
        passData.add(MatisseInit.PASS_LOG, new StringBuilder());

        return passData;
    }
}
