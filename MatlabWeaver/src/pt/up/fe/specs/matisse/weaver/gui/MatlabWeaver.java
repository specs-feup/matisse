/*
 * Copyright 2015 SpecS.
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

package pt.up.fe.specs.matisse.weaver.gui;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.matisse.weaver.MWeaver;

/**
 * Executes the MatlabWeaver.
 *
 * @author Joao Bispo
 */
public class MatlabWeaver {

    private final DataStore data;

    public MatlabWeaver(DataStore data) {
        this.data = data;
    }

    public int execute() {

        // Build arguments
        List<String> args = buildArgs();
        boolean success = MWeaver.run(args.toArray(new String[args.size()]), MatlabToCOptionUtils.newDefaultSettings());

        if (!success) {
            return 1;
        }

        return 0;

    }

    private List<String> buildArgs() {
        List<String> args = new ArrayList<>();

        // Input file
        args.add(data.get(MWeaverOption.INPUT_FILE).getAbsolutePath());

        args.add("-b");
        String level = Integer.toString(data.get(MWeaverOption.VERBOSE_LEVEL).getLevel());
        args.add(level);

        // Always output javascript to log file
        args.add("-j"); // <-- We need to discuss the usage of this argument

        // Application folder
        data.getTry(MWeaverOption.WORKSPACE)
                .ifPresent(workspace -> {
                    args.add("-p");
                    args.add(workspace.getAbsolutePath());
                });

        // Weaver mode
        if (data.get(MWeaverOption.WEAVER_MODE) == WeaverMode.TOM) {
            args.add("-aw");
            args.add("tom");
        }

        // Aspect arguments
        if (data.hasValue(MWeaverOption.MAIN_ASPECT_ARGS)) {
            args.add("-av");
            for (String arg : data.get(MWeaverOption.MAIN_ASPECT_ARGS)) {
                args.add(arg);
            }
        }

        // Debug
        if (data.get(MWeaverOption.DEBUG)) {
            args.add("-d");
        }

        // Includes
        if (data.hasValue(MWeaverOption.INCLUDES_FOLDER)) {
            args.add("-i");
            args.add(data.get(MWeaverOption.INCLUDES_FOLDER).getAbsolutePath());
        }

        // JS output
        if (data.get(MWeaverOption.SHOW_JS_OUTPUT)) {
            args.add("-j");
        }

        // Log
        if (data.get(MWeaverOption.LOG)) {
            args.add("-l");

            String logFile = data.getTry(MWeaverOption.LOG_FILENAME).orElse("mweaver.log");
            args.add(logFile);
        }

        // Main aspect
        data.getTry(MWeaverOption.MAIN_ASPECT)
                .ifPresent(aspect -> {
                    args.add("-m");
                    args.add(aspect);
                });

        // Output folder
        data.getTry(MWeaverOption.OUTPUT)
                .ifPresent(output -> {
                    args.add("-o");
                    args.add(output.getAbsolutePath());
                });

        // Output file
        data.getTry(MWeaverOption.REPORT)
                .ifPresent(report -> {
                    args.add("-r");
                    args.add(report);
                });

        // Tools XML
        data.getTry(MWeaverOption.TOOLS)
                .ifPresent(tools -> {
                    args.add("-t");
                    args.add(tools);
                });

        // Weaver class
        data.getTry(MWeaverOption.WEAVER)
                .ifPresent(weaver -> {
                    args.add("-w");
                    args.add(weaver);
                });

        // XML Specification
        data.getTry(MWeaverOption.XML_SPEC)
                .ifPresent(xmlSpecs -> {
                    args.add("-x");
                    args.add(xmlSpecs.getAbsolutePath());
                });

        // System.out.println("ARGS:" + args);
        return args;
    }
}
