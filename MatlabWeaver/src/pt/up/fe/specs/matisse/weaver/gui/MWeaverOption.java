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

package pt.up.fe.specs.matisse.weaver.gui;

import java.io.File;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import pt.up.fe.specs.util.utilities.StringList;

public class MWeaverOption {

    /**
     * <larafile>.lara|<aspIR>.xml
     */
    public static final DataKey<File> INPUT_FILE = KeyFactory.object("input_file", File.class);

    /**
     * Select TOM or Java mode.
     */
    public static final DataKey<WeaverMode> WEAVER_MODE = KeyFactory.object("weaver_mode", WeaverMode.class);

    /**
     * arguments for the main aspect (optional)
     */
    public static final DataKey<StringList> MAIN_ASPECT_ARGS = KeyFactory.object("main_aspect_args", StringList.class)
	    .setDefault(() -> new StringList());
    // public static final DataKey<StringList> WEAVER_ARGS = KeyFactory.object("weaver_args", StringList.class,
    // new StringList());

    /**
     * verbose level
     */
    public static final DataKey<VerboseLevel> VERBOSE_LEVEL = KeyFactory.object("verbose_level", VerboseLevel.class)
	    .setDefault(() -> VerboseLevel.ERROR);
    /**
     * show all process information
     */
    public static final DataKey<Boolean> DEBUG = KeyFactory.bool("debug");

    /**
     * location of the include files(.jar,.js,.class) (optional)
     */
    public static final DataKey<File> INCLUDES_FOLDER = KeyFactory.object("includes", File.class);

    /**
     * show the javascript output in the same stream as the application's output
     */
    public static final DataKey<Boolean> SHOW_JS_OUTPUT = KeyFactory.bool("show_js_output");

    /**
     * outputs to a log file
     */
    public static final DataKey<Boolean> LOG = KeyFactory.bool("log");

    /**
     * the name of the log file (optional)
     */
    public static final DataKey<String> LOG_FILENAME = KeyFactory.object("log_file", String.class);

    /**
     * select main aspect (optional)
     */
    public static final DataKey<String> MAIN_ASPECT = KeyFactory.string("main_aspect");

    /**
     * change output dir (optional)
     */
    public static final DataKey<File> OUTPUT = KeyFactory.object("output", File.class);

    /**
     * change the working directory (optional)
     */
    public static final DataKey<File> WORKSPACE = KeyFactory.object("workspace", File.class);

    /**
     * Output file for the output of the main aspect, in a JSON format (optional)
     */
    public static final DataKey<String> REPORT = KeyFactory.string("report");

    /**
     * location of the tools' run description (tools.xml) (optional)
     */
    public static final DataKey<String> TOOLS = KeyFactory.string("tools");

    /**
     * change the target weaver (default: org.lara.interpreter.defaultweaver.Default Weaver) (optional)
     */
    public static final DataKey<String> WEAVER = KeyFactory.string("weaver");

    /**
     * location of the target language specification folder (optional)
     */
    public static final DataKey<File> XML_SPEC = KeyFactory.object("xml_spec", File.class);
}
