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

package org.specs.matlabprocessorprinter.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CProject;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Passes.CRecipe;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.MFileInstance.MatlabToCEngine;
import org.specs.MatlabToC.MFunctions.MFunctionPrototype;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.MatlabToC.SystemInfo.ProjectMFiles;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matisselib.DefaultRecipes;
import org.specs.matlabprocessorprinter.CodeGenerationSettings;
import org.specs.matlabprocessorprinter.ContentPage;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.io.SimpleFile;

public class OldCCodePrinter implements CodePrinter {

    @Override
    public List<ContentPage> getCode(String code,
            DataStore setup,
            FileNode file,
            PrintStream reportStream,
            CodeGenerationSettings settings) throws IOException {

        FunctionFileNode functionFile = (FunctionFileNode) file;

        String functionName = functionFile.getMainFunctionName();
        String filename = file.getMainUnitName() + ".m";
        FunctionNode function = functionFile.getMainFunction();

        TypesMap types = setup.get(MatisseKeys.TYPE_DEFINITION);

        ImplementationData implementationData = MatlabToCUtils.newImplementationData(settings.getLanguageMode(), types,
                setup);
        ProjectMFiles projectMFiles = implementationData.getProjectMFiles();
        projectMFiles.addUserFile(SimpleFile.newInstance(filename, file.getCode()));

        MatlabToCEngine engine = MFileProvider.buildEngine(setup, settings.getLanguageMode());
        MFileProvider.setEngine(engine);

        MFunctionPrototype mFunctionProto = MFunctionPrototype.newMainFunction(false, filename, functionName,
                function, implementationData);

        // FunctionInstance implementation = MatlabToCUtils.buildImplementation(mFunctionProto,
        FunctionInstance implementation = MatlabToCUtils.buildImplementation(
                mFunctionProto,
                types,
                setup);

        CProject cProject = new CProject(settings.applyPasses() ? DefaultRecipes.DefaultCRecipe : CRecipe.empty(),
                setup);
        cProject.addFunction(implementation);

        String result = CCodePrinter.getCodeForProject(cProject);
        return Arrays.asList(new ContentPage("C code", result, getContentType()));
    }

    public String getContentType() {
        return "text/java";
    }

    @Override
    public void processSetup(DataStore basicSetup, CodeGenerationSettings settings) {
        basicSetup.set(MatlabToCKeys.USE_PASS_SYSTEM, false);
    }
}
