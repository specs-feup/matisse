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
import org.specs.matisselib.DefaultReportService;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.CommonInstructionReportingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.MockWideScopeService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBuilder;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.providers.StringProvider;

public class BytecodeCodePrinter implements CodePrinter {

    @Override
    public List<ContentPage> getCode(String code, DataStore setup,
            FileNode file, PrintStream reportStream,
            CodeGenerationSettings settings) {

        WideScopeService projectWideScope = new MockWideScopeService(file);

        StringBuilder builder = new StringBuilder();

        for (MatlabNode function : file.getFunctions()) {

            String functionName = ((FunctionNode) function).getFunctionName();
            FunctionIdentification functionIdentification = new FunctionIdentification(file.getFilename() + ".m",
                    functionName);

            DefaultReportService defaultReporter = new DefaultReportService(null,
                    reportStream,
                    false,
                    functionIdentification,
                    StringProvider.newInstance(code));

            CommonPassData passData = new CommonPassData("dummy");
            passData.add(PassManager.NODE_REPORTING,
                    new NodeReportService(reportStream, "file.m", StringProvider.newInstance(code)));

            WideScopeService wideScope = projectWideScope
                    .withFunctionIdentification(projectWideScope
                            .getUserFunction(
                                    functionIdentification,
                                    functionName)
                            .get());
            passData.add(PreTypeInferenceServices.COMMON_NAMING, new CommonNamingService(wideScope, file));
            passData.add(TypeInferencePass.INSTRUCTION_REPORT_SERVICE, new CommonInstructionReportingService(
                    defaultReporter));
            passData.add(PassManager.DIRECTIVE_PARSER, new DirectiveParser());

            for (MatlabNodePass pass : DefaultRecipes.DefaultMatlabASTTypeInferenceRecipe.getPasses()) {
                function = pass.apply(function, passData);
            }

            FunctionBody body = SsaBuilder.buildSsa((FunctionNode) function, passData);

            if (settings.applyPasses()) {
                for (SsaPass pass : DefaultRecipes.getTestPreTypeInferenceRecipe().getPasses()) {
                    pass.apply(body, passData);
                }
            }

            builder.append(body);
        }

        return Arrays.asList(new ContentPage("IR", builder.toString(), getContentType()));
    }

    public String getContentType() {
        return "application/x-matisse-bytecode";
    }

}
