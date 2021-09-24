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

package pt.up.fe.specs.matisse.weaver;

import org.lara.interpreter.joptions.gui.LaraLauncher;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class MWeaverLauncher {

    public static boolean execute(String[] args) {
        return execute(args, MatlabToCOptionUtils.newDefaultSettings());
    }

    public static boolean execute(String[] args, DataStore settings) {
        // MatisseSetup setup = MatlabToCOptionUtils.newDefaultSetup();

        return LaraLauncher.launch(args, new MWeaver(settings));
    }

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        try {
            boolean success = execute(args);
            if (!success) {
                SpecsLogs.msgInfo("LARAI execution returned false");
                System.exit(1);
            }
        } catch (RuntimeException e) {
            SpecsLogs.msgInfo("Exception while running Matlab Weaver: " + e.getMessage());
            System.exit(1);
        }

    }
}
