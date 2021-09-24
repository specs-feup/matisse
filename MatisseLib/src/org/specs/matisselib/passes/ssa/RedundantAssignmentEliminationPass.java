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

package org.specs.matisselib.passes.ssa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.PhiInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

public class RedundantAssignmentEliminationPass implements SsaPass, PostTypeInferencePass {
    public static DataKey<Boolean> CAN_FUSE_NAMES_KEY = KeyFactory.bool("can-fuse-names");

    private final boolean canFuseNames;

    public RedundantAssignmentEliminationPass(boolean canFuseNames) {
        this.canFuseNames = canFuseNames;
    }

    public RedundantAssignmentEliminationPass(DataView parameters) {
        if (!parameters.hasValue(RedundantAssignmentEliminationPass.CAN_FUSE_NAMES_KEY)) {
            throw new RuntimeException("Parameter can-fuse-names is missing.");
        }

        this.canFuseNames = parameters.getValue(RedundantAssignmentEliminationPass.CAN_FUSE_NAMES_KEY);
    }

    static interface MergeAction {
        public void apply(String a, String b, String merged);
    }

    @Override
    public void apply(FunctionBody source, DataStore data) {
        Preconditions.checkArgument(source != null);
        Preconditions.checkArgument(data != null);

        apply(source,
                (a, b) -> true,
                (a, b, merged) -> {
                });
    }

    @Override
    public void apply(TypedInstance source, DataStore data) {
        Preconditions.checkArgument(source != null);
        Preconditions.checkArgument(data != null);

        apply(source.getFunctionBody(),
                // Can only merge variables of identical types.
                (a, b) -> source.getVariableType(a).equals(source.getVariableType(b)),
                (a, b, merged) -> {
                    // FIXME: What about shapes?
                    Optional<VariableType> candidateType = source.getVariableType(a);
                    Optional<VariableType> otherType = source.getVariableType(b);

                    candidateType.ifPresent(type -> {

                        VariableType mergedType = type;

                        if (!ScalarUtils.hasConstant(type)
                                && otherType.isPresent()
                                && ScalarUtils.hasConstant(otherType.get())) {

                            mergedType = ((ScalarType) type).scalar()
                                    .setConstantString(ScalarUtils.getConstantString(otherType.get()));
                        }

                        source.addOrOverwriteVariable(merged, mergedType);
                    });
                });
    }

    private void apply(FunctionBody source,
            BiPredicate<String, String> canMerge,
            MergeAction mergeAction) {

        while (tryApply(source, canMerge, mergeAction)) {

        }
    }

    private boolean tryApply(FunctionBody body,
            BiPredicate<String, String> canMerge,
            MergeAction mergeAction) {

        boolean hasChanges = false;

        for (SsaBlock block : body.getBlocks()) {
            ListIterator<SsaInstruction> listIterator = block.getInstructions().listIterator();
            while (listIterator.hasNext()) {
                SsaInstruction instruction = listIterator.next();
                if (instruction instanceof AssignmentInstruction) {
                    AssignmentInstruction assignment = (AssignmentInstruction) instruction;
                    if (assignment.getInput() instanceof VariableInput) {
                        String input = ((VariableInput) assignment.getInput()).getName();
                        String output = assignment.getOutput();

                        if (canMerge.test(input, output)) {
                            Optional<String> mergedName = getMergedName(output, input);
                            if (mergedName.isPresent()) {
                                String newName = mergedName.get();

                                hasChanges = true;

                                applyMerge(body, mergeAction, listIterator, input, output, newName);
                            }
                        }
                    }
                    continue;
                }

                if (instruction instanceof PhiInstruction) {
                    PhiInstruction phi = (PhiInstruction) instruction;

                    String output = phi.getOutput();
                    String input = getEffectiveSingleInput(phi);
                    if (input != null && canMerge.test(input, output)) {
                        Optional<String> mergedName = getMergedName(output, input);
                        if (mergedName.isPresent()) {
                            String newName = mergedName.get();

                            hasChanges = true;

                            applyMerge(body, mergeAction, listIterator, input, output, newName);
                        }
                    }
                }
            }
        }

        return hasChanges;
    }

    private static String getEffectiveSingleInput(PhiInstruction phi) {
        String effectiveInput = null;
        for (String input : phi.getInputVariables()) {
            if (input.equals(phi.getOutput())) {
                continue;
            }
            if (effectiveInput == null) {
                effectiveInput = input;
            } else if (!input.equals(effectiveInput)) {
                return null;
            }
        }

        assert effectiveInput != null;
        return effectiveInput;
    }

    private static void applyMerge(FunctionBody body,
            MergeAction mergeAction,
            ListIterator<SsaInstruction> listIterator,
            String input,
            String output,
            String newName) {

        mergeAction.apply(output, input, newName);

        Map<String, String> newNames = new HashMap<>();
        newNames.put(output, newName);
        newNames.put(input, newName);
        body.renameVariables(newNames);
        listIterator.remove();
    }

    private Optional<String> getMergedName(String outputName, String inputName) {
        assert !outputName.equals(inputName) : "SSA semantics violation";

        int outputSeparator = outputName.indexOf('$');
        assert outputSeparator != -1;
        int inputSeparator = inputName.indexOf('$');
        assert inputSeparator != -1;

        String output1 = outputName.substring(0, outputSeparator);
        String output2 = outputName.substring(outputSeparator + 1);

        String input1 = inputName.substring(0, inputSeparator);
        String input2 = inputName.substring(inputSeparator + 1);

        boolean outputIsAnonymous = output1.isEmpty();
        boolean inputIsAnonymous = input1.isEmpty();

        boolean isReturnOutput = !outputIsAnonymous && output2.equals("ret");
        boolean isReturnInput = !inputIsAnonymous && input2.equals("ret");

        if (isReturnOutput && isReturnInput) {
            // Both are returned variables, so we can't erase any of them.
            return Optional.empty();
        }

        if (isReturnOutput) {
            if (inputIsAnonymous || input1.equals(output1) || this.canFuseNames) {
                return Optional.of(outputName);
            }
            return Optional.empty();
        }

        if (isReturnInput) {
            if (outputIsAnonymous || input1.equals(output1) || this.canFuseNames) {
                return Optional.of(inputName);
            }
            return Optional.empty();
        }

        // Neither are returned variables

        if (this.canFuseNames || inputIsAnonymous || outputIsAnonymous || input1.equals(output1)) {
            return Optional.of(getFusedName(input1, output1, input2, output2));
        }

        return Optional.empty();
    }

    private static String getFusedName(String input1, String output1, String input2, String output2) {
        if (!input1.isEmpty()) {
            if (!output1.isEmpty()) {

                String result1 = Stream.concat(
                        Stream.of(input1.split("\\+")),
                        Stream.of(output1.split("\\+")))
                        .distinct()
                        .collect(Collectors.joining("+"));

                String result2 = Stream.concat(
                        Stream.of(input2.split("\\+")),
                        Stream.of(output2.split("\\+")))
                        .distinct()
                        .collect(Collectors.joining("+"));

                return result1 + "$" + result2;
            }

            return input1 + "$" + input2;
        }
        if (!output1.isEmpty()) {
            return output1 + "$" + output2;
        }

        // Both variables are temporaries.
        // We haven't much to go on in that case. Temporaries start with '$' and can't end in '$end', but other than
        // that anything goes.
        // However, *usually* they'll be something like '$semantics$number'.
        // We'll just return the the first temporary for now and we'll figure something out later on.
        return "$" + input2;
    }

    @Override
    public String getName() {
        return SsaPass.super.getName();
    }

    @Override
    public DataView getParameters() {
        DataStore store = new SimpleDataStore("rae-params");

        store.add(RedundantAssignmentEliminationPass.CAN_FUSE_NAMES_KEY, this.canFuseNames);

        return DataView.newInstance(store);
    }

    public static List<DataKey<?>> getRequiredParameters() {
        return Arrays.asList(RedundantAssignmentEliminationPass.CAN_FUSE_NAMES_KEY);
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
