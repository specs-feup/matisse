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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.expression.F;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.CompilerDataProviders;
import org.specs.matisselib.PassUtils;
import org.specs.matisselib.helpers.ConstantUtils;
import org.specs.matisselib.loopproperties.EstimatedIterationsProperty;
import org.specs.matisselib.services.DataService;
import org.specs.matisselib.services.Logger;
import org.specs.matisselib.ssa.NumberInput;
import org.specs.matisselib.ssa.VariableInput;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matisselib.ssa.instructions.ForInstruction;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class SymjaConstantBuilderPass implements PostTypeInferencePass {

    public static final String PASS_NAME = "symja_constant_builder";

    @Override
    public void apply(TypedInstance instance, DataStore passData) {

        Logger logger = PassUtils.getLogger(passData, PASS_NAME);
        if (PassUtils.skipPass(instance, PASS_NAME)) {
            logger.logSkip(instance);
            return;
        }

        Map<String, String> assignedNames = new HashMap<>();

        logger.logStart(instance);

        F.initSymbols();
        EvalUtilities util = new EvalUtilities();

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {
            boolean fixedOutput = false;

            for (String output : instruction.getOutputs()) {
                Optional<Number> constant = instance
                        .getVariableType(output)
                        .filter(ScalarType.class::isInstance)
                        .map(ScalarUtils::getConstant)
                        .filter(k -> k.intValue() == k.doubleValue());

                fixedOutput |= constant.isPresent();
                constant.ifPresent(k -> {

                    StringBuilder dataBuilder = new StringBuilder();
                    dataBuilder.append(buildVar(assignedNames, output));
                    dataBuilder.append(" = ");
                    dataBuilder.append(k.intValue());

                    addExpr(util, dataBuilder);
                });
            }

            if (fixedOutput) {
                continue;
            }

            if (instruction instanceof AssignmentInstruction) {
                AssignmentInstruction assignment = (AssignmentInstruction) instruction;

                String output = assignment.getOutput();
                if (!ScalarUtils.isScalar(instance.getVariableType(output))) {
                    continue;
                }

                StringBuilder dataBuilder = new StringBuilder();
                if (assignment.getInput() instanceof VariableInput) {
                    dataBuilder.append(buildVar(assignedNames, assignment.getOutput()));
                    dataBuilder.append(" = ");
                    dataBuilder.append(buildVar(assignedNames, assignment.getInputVariables().get(0)));
                    addExpr(util, dataBuilder);
                } else if (assignment.getInput() instanceof NumberInput) {
                    Number number = ((NumberInput) assignment.getInput()).getNumber();

                    if (number.intValue() == number.doubleValue()) {
                        dataBuilder.append(buildVar(assignedNames, assignment.getOutput()));
                        dataBuilder.append(" = ");
                        dataBuilder.append(number.intValue());
                        addExpr(util, dataBuilder);
                    }
                }

                continue;
            }
            if (instruction instanceof FunctionCallInstruction) {
                FunctionCallInstruction functionCall = (FunctionCallInstruction) instruction;

                if (functionCall.getOutputs().size() != 1) {
                    continue;
                }

                String output = functionCall.getOutputs().get(0);
                if (!ScalarUtils.isScalar(instance.getVariableType(output))) {
                    continue;
                }

                if (functionCall.getFunctionName().equals("plus") && functionCall.getInputVariables().size() == 2) {
                    StringBuilder dataBuilder = new StringBuilder();
                    dataBuilder.append(buildVar(assignedNames, output));
                    dataBuilder.append(" = ");
                    dataBuilder.append(buildVar(assignedNames, functionCall.getInputVariables().get(0)));
                    dataBuilder.append(" + ");
                    dataBuilder.append(buildVar(assignedNames, functionCall.getInputVariables().get(1)));
                    addExpr(util, dataBuilder);
                    continue;
                }
                if (functionCall.getFunctionName().equals("minus") && functionCall.getInputVariables().size() == 2) {
                    StringBuilder dataBuilder = new StringBuilder();
                    dataBuilder.append(buildVar(assignedNames, output));
                    dataBuilder.append(" = ");
                    dataBuilder.append(buildVar(assignedNames, functionCall.getInputVariables().get(0)));
                    dataBuilder.append(" - ");
                    dataBuilder.append(buildVar(assignedNames, functionCall.getInputVariables().get(1)));
                    addExpr(util, dataBuilder);
                    continue;
                }

                continue;
            }

        }

        for (SsaInstruction instruction : instance.getFlattenedInstructionsIterable()) {

            if (instruction instanceof ForInstruction) {
                ForInstruction xfor = (ForInstruction) instruction;

                if (xfor.getLoopProperties().stream().anyMatch(EstimatedIterationsProperty.class::isInstance)) {
                    continue;
                }

                if (!ConstantUtils.isConstantOne(instance, xfor.getInterval())) {
                    continue;
                }

                String expr = buildVar(assignedNames, xfor.getEnd()) + " - " + buildVar(assignedNames, xfor.getStart())
                        + " + 1";
                String result = tryEval(assignedNames, util, expr);
                logger.log("Loop iterations: " + result);
                if (result.matches("^[+-]?[0-9]+$")) {
                    logger.log("Deducing number of iterations: " + result);

                    int numIters = Integer.parseInt(result, 10);
                    if (numIters < 0)
                        numIters = 0;

                    xfor.addLoopProperty(new EstimatedIterationsProperty(numIters));
                }

                continue;
            }

            for (String output : instruction.getOutputs()) {
                Optional<VariableType> type = instance.getVariableType(output);

                if (!ScalarUtils.isScalar(type) || ScalarUtils.hasConstant(type)) {
                    continue;
                }

                String result = tryEval(assignedNames, util, buildVar(assignedNames, output));

                logger.log("Result of " + output + ": " + result);
                if (result.matches("^[+-]?[0-9]+$")) {
                    logger.log("Deducing value of " + output + ": " + result);

                    VariableType newType = ScalarUtils.setConstantString(type.get(), result);
                    instance.addOrOverwriteVariable(output, newType);
                }

            }
        }
    }

    private String tryEval(Map<String, String> assignedNames, EvalUtilities util, String var) {
        String result;
        try {
            result = util.evaluate(var).fullFormString();
        } catch (Exception e) {
            result = "<INVALID>";
            System.err.println(e);
            e.printStackTrace();
        }
        return result;
    }

    private void addExpr(EvalUtilities util, StringBuilder dataBuilder) {
        try {
            // System.out.println("SYMJA");
            // System.out.println(dataBuilder);
            util.evaluate(dataBuilder.toString());
        } catch (Exception e1) {
            System.err.println(e1);
            e1.printStackTrace();
        }
    }

    private String buildVar(Map<String, String> assignedNames, String var) {
        if (assignedNames.containsKey(var)) {
            return assignedNames.get(var);
        }

        String baseVarName = "v" + var.replaceAll("[$_+]", "").toLowerCase(Locale.UK);

        int i = assignedNames.size();
        String proposedName = baseVarName;
        while (assignedNames.containsValue(proposedName)) {
            proposedName = baseVarName + ++i;
        }

        assignedNames.put(var, proposedName);
        return proposedName;
    }

    @Override
    public boolean preserveData(DataService<?> key) {
        return PassUtils.approveIn(key,
                CompilerDataProviders.CONTROL_FLOW_GRAPH,
                CompilerDataProviders.SIZE_GROUP_INFORMATION);
    }
}
