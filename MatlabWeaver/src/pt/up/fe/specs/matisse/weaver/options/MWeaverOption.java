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

package pt.up.fe.specs.matisse.weaver.options;

import java.util.List;

import org.lara.interpreter.weaver.options.OptionArguments;
import org.lara.interpreter.weaver.options.WeaverOption;
import org.lara.interpreter.weaver.options.WeaverOptionBuilder;
import org.suikasoft.jOptions.Datakey.DataKey;

public enum MWeaverOption {
    LANGUAGE("L", "Language", MWeaverKeys.LANGUAGE, OptionArguments.ONE_ARG, "language"),
    // MATLABTYPES("M", "Enable MatlabTypes", MWeaverKeys.MATLABTYPES),
    CHECK_SYNTAX("S", "Check Syntax", MWeaverKeys.CHECK_SYNTAX),

    AUTOMATIC_CODE_GENERATION("C", "Automatic Code Generation", MWeaverKeys.AUTOMATIC_CODE_GENERATION),

    DISABLE_CODE_PARSING("P", "Disable Code Parsing", MWeaverKeys.DISABLE_CODE_PARSING);

    // TOM("T", "Use TOM (deprecated!", MWeaverKeys.TOM);

    private final String shortOption;
    private final String description;
    private final DataKey<?> dataKey;
    private final OptionArguments optionArguments;
    private final String argumentName;

    private MWeaverOption(String shortOption, String description, DataKey<?> dataKey) {
        this(shortOption, description, dataKey, OptionArguments.NO_ARGS, "");
    }

    private MWeaverOption(String shortOption, String description, DataKey<?> dataKey, OptionArguments optionArguments,
            String argumentName) {
        this.shortOption = shortOption;
        this.description = description;
        this.dataKey = dataKey;
        this.optionArguments = optionArguments;
        this.argumentName = argumentName;
    }

    public WeaverOption getOption() {
        return WeaverOptionBuilder.build(shortOption, name().toLowerCase(), optionArguments, argumentName, description,
                dataKey);
    }

    public static List<WeaverOption> getOptions() {
        return WeaverOptionBuilder.enum2List(MWeaverOption.class, MWeaverOption::getOption);
    }

}
