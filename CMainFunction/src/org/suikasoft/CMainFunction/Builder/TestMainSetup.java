/**
 * Copyright 2013 SPeCS Research Group.
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

package org.suikasoft.CMainFunction.Builder;

import java.util.List;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice;
import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * Setup for class TestMainOptions, in CIR.
 * 
 * @author Joao Bispo
 * 
 */
public enum TestMainSetup implements SetupFieldEnum, DefaultValue, MultipleChoice {

    GenerateMainForCoder(FieldType.bool),
    PrintExecutionTime(FieldType.bool),
    WriteOutputs(FieldType.bool),
    Warmup(FieldType.bool),
    // AddCodeToMeasureExecution(FieldType.bool),
    Target(FieldType.multipleChoice),
    // EnableSuccessiveExecutions(FieldType.bool),
    // PrintOutputs(FieldType.bool),
    // ReturnOutputs(FieldType.bool),
    ExtraMainPreparationCode(FieldType.string),
    ExtraMainFinalizationCode(FieldType.string),
    ExtraMainFilePrefixCode(FieldType.string),
    ExtraMainAfterWarmupCode(FieldType.string);

    public static TestMainOptions newData(SetupData setupData) {

        SetupAccess setup = new SetupAccess(setupData);

        // optionsForMainFunction.put(TestMainSetup.EnableSuccessiveExecutions, true);
        // optionsForMainFunction.put(TestMainSetup.AddCodeToMeasureExecution, true);
        // optionsForMainFunction.put(TestMainSetup.PrintOutputs, false);

        boolean generateMainForCoder = setup.getBoolean(GenerateMainForCoder);

        boolean printExecutionTime = setup.getBoolean(PrintExecutionTime);

        boolean cycleMeasuringPrints;
        boolean enableSuccessiveExecutions;
        boolean printOutputs;
        boolean returnOutputs;
        if (printExecutionTime) {
            cycleMeasuringPrints = true;
            enableSuccessiveExecutions = true;
            printOutputs = false;
            returnOutputs = true;
        } else {
            cycleMeasuringPrints = false;
            enableSuccessiveExecutions = false;
            printOutputs = true;
            returnOutputs = false;
        }

        boolean writeOutputs = setup.getBoolean(WriteOutputs);
        boolean warmup = setup.getBoolean(Warmup);

        MainFunctionTarget target = setup.getEnum(Target, MainFunctionTarget.class);
        // TimeMeasurer timeMeasurer = target;
        // TimeMeasurer timeMeasurer = new KernelTimeFactory(data);
        // boolean cycleMeasuringPrints = setup.getBoolean(AddCodeToMeasureExecution);
        // boolean enableSuccessiveExecutions = setup.getBoolean(EnableSuccessiveExecutions);
        // boolean printOutputs = setup.getBoolean(PrintOutputs);
        // boolean returnOutputs = setup.getBoolean(ReturnOutputs);

        String extraMainPreparationCode = setup.getString(ExtraMainPreparationCode);
        String extraMainFinalizationCode = setup.getString(ExtraMainFinalizationCode);
        String extraMainFilePrefixCode = setup.getString(ExtraMainFilePrefixCode);
        String extraMainAfterWarmupCode = setup.getString(ExtraMainAfterWarmupCode);

        return new TestMainOptions(generateMainForCoder,
                cycleMeasuringPrints,
                target,
                enableSuccessiveExecutions,
                printOutputs,
                returnOutputs,
                writeOutputs,
                warmup,
                extraMainPreparationCode,
                extraMainFinalizationCode,
                extraMainFilePrefixCode,
                extraMainAfterWarmupCode);
    }

    private TestMainSetup(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
        return this.fieldType;
    }

    @Override
    public String getSetupName() {
        return "Options for main() function";
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {
        if (this == WriteOutputs) {
            return FieldValue.create(Boolean.TRUE, this.fieldType);
        }
        /*
        	if(this == CycleMeasuringPrintsForMicroBlazeSimulator) {
        	    return FieldValue.create(Boolean.TRUE, this.getType());
        	}
        */
        // if (this == PrintOutputs) {
        // return FieldValue.create(Boolean.TRUE, this.getType());
        // }

        // if (this == ExtraMainPreparationCode) {
        // return FieldValue.create("", this.getType());
        // }

        return null;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice#getChoices()
     */
    @Override
    public StringList getChoices() {

        if (this == Target) {
            List<String> names = SpecsEnums.extractNames(MainFunctionTarget.class);
            return new StringList(names);
        }

        return null;
    }

}
