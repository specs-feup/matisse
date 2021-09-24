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

package org.specs.matlabprocessorprinter.printers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.MockWideScopeService;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.providers.StringProvider;

public abstract class DirectCodePrinter implements CodePrinter {
    @Override
    public List<ContentPage> getCode(String code,
            DataStore setup,
            FileNode file,
            PrintStream reportStream,
            CodeGenerationSettings settings) {

        WideScopeService projectWideScope = new MockWideScopeService(file);

        for (MatlabNode function : file.getFunctions()) {
            FunctionNode functionNode = ((FunctionNode) function);
            String functionName = functionNode.getFunctionName();

            if (settings.applyPasses()) {
                CommonPassData passData = new CommonPassData("dummy");

                passData.add(PassManager.NODE_REPORTING,
                        new NodeReportService(reportStream, "file.m", StringProvider.newInstance(code)));
                WideScopeService wideScope = projectWideScope
                        .withFunctionIdentification(projectWideScope
                                .getUserFunction(
                                        new FunctionIdentification(file.getFilename() + ".m", functionName),
                                        functionName)
                                .get());
                passData.add(PreTypeInferenceServices.COMMON_NAMING, new CommonNamingService(wideScope, file));

                for (MatlabNodePass pass : DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe.getPasses()) {
                    function = pass.apply(function, passData);
                }

                assert functionNode == function;
            }
        }

        String astCode = getAstCode(file);

        return Arrays.asList(new ContentPage("AST", astCode, getContentType()));
    }

    protected abstract String getContentType();

    protected abstract String getAstCode(FileNode file);
}
