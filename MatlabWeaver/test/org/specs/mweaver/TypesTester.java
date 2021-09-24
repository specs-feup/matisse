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

package org.specs.mweaver;

import java.io.File;

import org.junit.Test;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;

import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class TypesTester {

    @Test
    public void test() {
        File workspace = SpecsIo.mkdir("run");
        File outputDir = new File(workspace, "output");
        File logFile = new File(workspace, "log.txt");

        ResourceProvider laraResource = TestResource.TYPES_LARA;
        File laraFile = new File(workspace, laraResource.getResourceName());
        SpecsIo.write(laraFile, SpecsIo.getResource(laraResource));

        // IoUtils.write(new File(workspace, "dummy.m"), "function dummy()\n a = 0;\n end");
        SpecsIo.write(new File(workspace, "dummy.m"), "a = 0");

        // m file "a = 0" does not work, raises an exception

        MWeaverUtils.run(workspace, outputDir, logFile, laraFile, MatlabToCOptionUtils.newDefaultSettings());

        SpecsIo.deleteFolderContents(workspace);
    }

}
