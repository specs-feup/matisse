/**
 * Copyright 2017 SPeCS.
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

package org.specs.matisselib.passes.posttype;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.ssa.instructions.TypedFunctionCallInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * 
 * Converts loop ranges into '1:X:Y' form.
 * 
 * @author Luís Reis
 *
 */
public class LoopStartNormalizationPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "loop_start_normalization";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);

        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.log("Skipping " + instance.getFunctionIdentification());
            return;
        }

        logger.log("Starting " + instance.getFunctionIdentification());

        SystemFunctionProviderService systemFunctions = passData.get(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);

        NumericFactory numerics = instance.getProviderData().getNumerics();

        List<SsaBlock> blocks = instance.getBlocks();
        for (int blockId = 0; blockId < blocks.size(); blockId++) {
            SsaBlock block = blocks.get(blockId);
            ForInstruction xfor = block.getEndingInstruction()
                    .filter(ForInstruction.class::isInstance)
                    .map(ForInstruction.class::cast)
                    .orElse(null);

            if (xfor == null) {
                continue;
            }

            logger.log("Inspecting loop at #" + blockId);

            String start = xfor.getStart();
            String end = xfor.getEnd();

            VariableType startType = instance.getVariableType(start).orElse(null);
            VariableType endType = instance.getVariableType(end).orElse(null);
            if (startType == null || endType == null) {
                // Undefined variable. It's not clear how to deal with this (it's probably an error).
                // So we'll just skip this pass.
                continue;
            }

            if (ConstantUtils.isConstantOne(startType)) {
                // No action needed.

                logger.log("Loop already starts at 1: " + xfor);
                continue;
            }

            logger.log("Transforming.");

            block.removeLastInstruction();

            BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, blockId);

            String one = editor.addMakeIntegerInstruction("one", 1);
            xfor.setStart(one);

            String offset = editor.addSimpleCallToOutput("minus", start, one);
            VariableType offsetType = instance.getVariableType(offset).get();
            String newEnd = editor.addSimpleCallToOutput("minus", end, offset);
            xfor.setEnd(newEnd);

            editor.addInstruction(xfor);

            int targetBlockId = xfor.getLoopBlock();
            SsaBlock targetBlock = blocks.get(targetBlockId);
            ListIterator<SsaInstruction> iterator = targetBlock.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();
                if (instruction instanceof IterInstruction) {
                    String output = ((IterInstruction) instruction).getOutput();

                    NumericTypeV2 intType = numerics.newInt();
                    String newIter = instance.makeTemporary("base_" + NameUtils.getSuggestedName(output),
                            intType);
                    iterator.set(new IterInstruction(newIter));

                    ProviderData plusData = instance.getProviderData()
                            .create(intType, offsetType);
                    FunctionType functionType = systemFunctions.getSystemFunction("plus").get()
                            .getType(plusData);
                    SsaInstruction functionCall = new TypedFunctionCallInstruction("plus", functionType,
                            Arrays.asList(output), Arrays.asList(newIter, offset));
                    iterator.add(functionCall);
                }
            }
        }
    }

}
