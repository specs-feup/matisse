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

/**
 * Options when generating the main file.
 * 
 * @author Joao Bispo
 * 
 */
public class TestMainOptions {

    public final boolean generateMainForCoder;
    public final boolean addCodeToMeasureExecution;
    public final MainFunctionTarget target;
    // public final TimeMeasurer timeMeasurer;
    public final boolean enableSuccessiveExecutions;
    public final boolean printOutputs;
    public final boolean returnOutputs;
    public final boolean writeOutputs;
    public final boolean warmup;
    public final String extraMainPreparationCode;
    public final String extraMainFinalizationCode;
    public final String extraMainFilePrefixCode;
    public final String extraMainAfterWarmupCode;

    public TestMainOptions(boolean generateMainForCoder,
            boolean cycleMeasuringPrints,
            MainFunctionTarget target,
            boolean enableSuccessiveExecutions,
            boolean printOutputs,
            boolean returnOutputs,
            boolean writeOutputs,
            boolean warmup,
            String extraMainPreparationCode,
            String extraMainFinalizationCode,
            String extraMainFilePrefixCode,
            String extraMainAfterWarmupCode) {

        this.generateMainForCoder = generateMainForCoder;
        this.addCodeToMeasureExecution = cycleMeasuringPrints;
        this.target = target;
        // this.timeMeasurer = timeMeasurer;
        this.enableSuccessiveExecutions = enableSuccessiveExecutions;
        this.printOutputs = printOutputs;
        this.returnOutputs = returnOutputs;
        this.writeOutputs = writeOutputs;
        this.warmup = warmup;
        this.extraMainPreparationCode = extraMainPreparationCode;
        this.extraMainFinalizationCode = extraMainFinalizationCode;
        this.extraMainFilePrefixCode = extraMainFilePrefixCode;
        this.extraMainAfterWarmupCode = extraMainAfterWarmupCode;
    }

}
