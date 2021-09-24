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
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.GetOrFirstInstruction;
import org.specs.matisselib.ssa.instructions.IterInstruction;
import org.specs.matisselib.ssa.instructions.SimpleGetInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Finds cases where the index of a get_or_first instruction if an iteration variable of a loop. If that loop is of the
 * form 1:1:N where N <= size of the matrix that was obtained, then the get_or_first can be converted into a simple_get.
 * <p>
 * Example:
 * 
 * <pre>
 * <code>for i = 1:numel(A),
 *    x = get_or_first(A, i);
 * </code>
 * 
 * @author Luís Reis
 *
 */
public class GetOrFirstSimplificationPass implements PostTypeInferencePass {

	public static final String PASS_NAME = "get_or_first_simplification";
	
    @Override
    public void apply(TypedInstance instance, DataStore passData) {
    	Logger logger = PassUtils.getLogger(passData, PASS_NAME);
    	
    	if (PassUtils.skipPass(instance, PASS_NAME)) {
    		logger.log("Skipping pass due to %!disable directive");
    		return;
    	}
    	
    	logger.log("Starting " + instance.getFunctionIdentification().getName());
    	
        SizeGroupInformation sizeInfo = passData.get(ProjectPassServices.DATA_PROVIDER)
                .buildData(CompilerDataProviders.SIZE_GROUP_INFORMATION);

        Map<Integer, String> acceptedLoopSizes = new HashMap<>();
        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                String start = xfor.getStart();
                String interval = xfor.getInterval();
                String end = xfor.getEnd();

                if (!ConstantUtils.isConstantOne(instance, start)) {
                    continue;
                }
                if (!ConstantUtils.isConstantOne(instance, interval)) {
                    continue;
                }

                acceptedLoopSizes.put(xfor.getLoopBlock(), end);
            }
        }
        
        logger.log("Loops: " + acceptedLoopSizes);

        Map<String, String> acceptedIters = new HashMap<>();
        for (int loopBlockId : acceptedLoopSizes.keySet()) {
            for (SsaInstruction instruction : instance.getBlock(loopBlockId).getInstructions()) {
                if (instruction instanceof IterInstruction) {
                    IterInstruction iter = (IterInstruction) instruction;

                    acceptedIters.put(iter.getOutput(), acceptedLoopSizes.get(loopBlockId));
                }
            }
        }

        for (SsaBlock block : instance.getBlocks()) {
            ListIterator<SsaInstruction> iterator = block.getInstructions().listIterator();
            while (iterator.hasNext()) {
                SsaInstruction instruction = iterator.next();

                if (instruction instanceof GetOrFirstInstruction) {
                    GetOrFirstInstruction getOrFirst = (GetOrFirstInstruction) instruction;

                    String index = getOrFirst.getIndex();
                    String referencedSize = acceptedIters.get(index);
                    if (referencedSize == null) {
                        logger.log("Index is not a valid loop iteration variable.");
                        continue;
                    }

                    String matrix = getOrFirst.getInputMatrix();
                    String matrixSize = sizeInfo.getNumelResult(matrix);
                    if (sizeInfo.areSameValue(referencedSize, matrixSize)) {
                    	logger.log("Optimizing: " + getOrFirst);
                    	
                        String output = getOrFirst.getOutput();
                        iterator.set(new SimpleGetInstruction(output, matrix, Arrays.asList(index)));
                    } else {
                        logger.log("Size mismatch between " + matrixSize + " and scalar " + referencedSize);
                    }
                }
            }
        }
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
