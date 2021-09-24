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

package org.specs.matlabtocl.v2.ssa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.TokenReportingService;
import org.specs.matisselib.ssa.BlockContext;
import org.specs.matisselib.ssa.instructions.AssignmentInstruction;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.codegen.ReductionType;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.CodeGenerationStrategyProvider;
import org.specs.matlabtocl.v2.codegen.reductionstrategies.SumReductionStrategy;
import org.specs.matlabtocl.v2.directives.generated.DirectivesLanguageLexer;
import org.specs.matlabtocl.v2.directives.generated.DirectivesLanguageParser;
import org.specs.matlabtocl.v2.directives.generated.DirectivesLanguageParser.ValueContext;
import org.specs.matlabtocl.v2.directives.generated.DirectivesLanguageParser.Value_parameterized_distribution_strategyContext;
import org.specs.matlabtocl.v2.loopproperties.NoIndexOverlapProperty;
import org.specs.matlabtocl.v2.loopproperties.SerialDimensionLoopProperty;
import org.specs.matlabtocl.v2.ssa.instructions.EndDirectiveInstruction;
import org.specs.matlabtocl.v2.ssa.instructions.ParallelDirectiveInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class ParallelDirectiveParser extends DirectiveParser {

    private static final boolean ENABLE_DIAGNOSTICS = false;

    private static final Pattern parallelDirectivePattern = Pattern.compile("^parallel\\b.*$");
    private static final Pattern endDirectivePattern = Pattern.compile("^end\\b.*$");

    @Override
    public void parseDirective(CommentSt stmt, BlockContext blockContext, DataStore dataStore) {
        TokenReportingService reportService = dataStore.get(PassManager.NODE_REPORTING);
        CodeGenerationStrategyProvider codeGenStrategy = dataStore.get(CLServices.CODE_GENERATION_STRATEGY_PROVIDER);

        String content = stmt.getCommentString();
        String directiveBody = content.substring(1).trim();// Remove leading '!'

        if (parallelDirectivePattern.matcher(directiveBody).matches()) {
            log("Found parallel directive");

            DirectivesLanguageLexer lexer = new DirectivesLanguageLexer(new ANTLRInputStream(directiveBody));
            DirectivesLanguageParser parser = new DirectivesLanguageParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());

            ParallelRegionSettings settings = new ParallelRegionSettings();
            settings.reductionStrategies = new HashMap<>(codeGenStrategy.getReductionStrategies());
            settings.localReductionStrategies = new HashMap<>(codeGenStrategy.getLocalReductionStrategies());
            settings.subgroupReductionStrategies = new HashMap<>(codeGenStrategy.getSubgroupReductionStrategies());

            blockContext.setLine(stmt.getLine());

            DirectivesLanguageParser.ParallelContext parallelContext;
            try {
                parallelContext = parser.parallel();
                DirectivesLanguageParser.Parallel_settingsContext settingsNode = parallelContext.parallel_settings();

                List<DirectivesLanguageParser.ScheduleContext> schedules = settingsNode.schedule();
                if (schedules.size() > 1) {
                    throw reportService.emitError(stmt, PassMessage.INVALID_DIRECTIVE_FORMAT,
                            "Multiple schedule clauses");
                }
                if (schedules.size() == 1) {
                    DirectivesLanguageParser.ScheduleContext distribution = schedules.get(0);
                    if (distribution.schedule_mode().AUTO() != null) {
                        settings.schedule = ScheduleStrategy.AUTO;
                    } else if (distribution.schedule_mode().DIRECT() != null) {
                        settings.schedule = ScheduleStrategy.DIRECT;
                    } else if (distribution.schedule_mode().COOPERATIVE() != null) {
                        settings.schedule = ScheduleStrategy.COOPERATIVE;
                    } else if (distribution.schedule_mode().SUBGROUP_COOPERATIVE() != null) {
                        settings.schedule = ScheduleStrategy.SUBGROUP_COOPERATIVE;
                    } else if (distribution.schedule_mode().value_parameterized_distribution_strategy() != null) {

                        List<String> values = parseValues(stmt, blockContext, "schedule_parameter",
                                distribution.schedule_mode().value(), reportService);

                        settings.schedule = getScheduleStrategy(
                                distribution.schedule_mode().value_parameterized_distribution_strategy(),
                                codeGenStrategy);
                        settings.scheduleNames = values;
                    } else {
                        throw new NotImplementedException(distribution.schedule_mode().getText());
                    }
                }

                List<DirectivesLanguageParser.Local_sizeContext> localSizes = settingsNode.local_size();
                if (localSizes.size() > 1) {
                    throw reportService.emitError(stmt, PassMessage.INVALID_DIRECTIVE_FORMAT,
                            "Multiple local_size clauses");
                }
                if (localSizes.size() == 1) {
                    DirectivesLanguageParser.Local_sizeContext localSize = localSizes.get(0);

                    List<String> values = parseValues(stmt, blockContext, "local_size_parameter", localSize.value(),
                            reportService);
                    settings.localSizes = values;
                }
                List<DirectivesLanguageParser.Sum_reduction_strategyContext> sumReductionStrategies = settingsNode
                        .sum_reduction_strategy();
                if (sumReductionStrategies.size() > 1) {
                    throw reportService.emitError(stmt, PassMessage.INVALID_DIRECTIVE_FORMAT,
                            "Multiple sum_reduction_strategy clauses");
                }
                if (sumReductionStrategies.size() == 1) {
                    DirectivesLanguageParser.Sum_reduction_strategyContext sumReductionStrategy = sumReductionStrategies
                            .get(0);
                    DirectivesLanguageParser.Sum_reduction_strategy_typeContext type = sumReductionStrategy
                            .sum_reduction_strategy_type();

                    SumReductionStrategy strategy;
                    if (type.SIMPLE() != null) {
                        strategy = SumReductionStrategy.SIMPLE_SUM_REDUCTION;
                    } else if (type.LOCAL_MEMORY() != null) {
                        strategy = SumReductionStrategy.LOCAL_MEMORY_SUM_REDUCTION;
                    } else if (type.WORKGROUP() != null) {
                        strategy = SumReductionStrategy.WORKGROUP_SUM_REDUCTION;
                    } else {
                        throw new NotImplementedException(type.getText());
                    }

                    settings.reductionStrategies.put(ReductionType.SUM, Arrays.asList(strategy.buildInstance()));
                }

                int disableRangeSet = settingsNode.DISABLE_RANGE_SET().size();
                if (disableRangeSet > 1) {
                    throw reportService.emitError(stmt, PassMessage.INVALID_DIRECTIVE_FORMAT,
                            "Multiple disable_range_set clauses");
                }
                if (disableRangeSet == 1) {
                    settings.rangeSetDisabled = true;
                }

            } catch (ParseCancellationException e) {
                Throwable cause = e.getCause();
                String message = e.getMessage();

                if (cause instanceof InputMismatchException) {
                    message = "Parse error at: '" + (((InputMismatchException) cause).getOffendingToken()).getText()
                            + "'.";
                }
                throw reportService.emitError(stmt, PassMessage.INVALID_DIRECTIVE_FORMAT,
                        "Error while parsing directive: " + message);
            }

            blockContext.addInstruction(new ParallelDirectiveInstruction(settings));
        } else if (endDirectivePattern.matcher(directiveBody).matches()) {
            log("Found end directive");

            blockContext.setLine(stmt.getLine());
            blockContext.addInstruction(new EndDirectiveInstruction());
        } else if (directiveBody.equals("serial_dimension")) {
            blockContext.loopProperties.add(new SerialDimensionLoopProperty());
        } else if (directiveBody.equals("no_index_overlap")) {
            blockContext.loopProperties.add(new NoIndexOverlapProperty());
        } else {
            super.parseDirective(stmt, blockContext, dataStore);
        }
    }

    private ScheduleStrategy getScheduleStrategy(Value_parameterized_distribution_strategyContext node,
            CodeGenerationStrategyProvider codeGenStrategy) {

        if (node.COARSE() != null) {
            return codeGenStrategy.getCoarseScheduleStrategy();
        }
        if (node.COARSE_SEQUENTIAL() != null) {
            return ScheduleStrategy.COARSE_SEQUENTIAL;
        }
        if (node.COARSE_GLOBAL_ROTATION() != null) {
            return ScheduleStrategy.COARSE_GLOBAL_ROTATION;
        }
        if (node.FIXED_WORK_GROUPS() != null) {
            return codeGenStrategy.getFixedWorkGroupsScheduleStrategy();
        }
        if (node.FIXED_WORK_GROUPS_SEQUENTIAL() != null) {
            return ScheduleStrategy.FIXED_WORK_GROUPS_SEQUENTIAL;
        }
        if (node.FIXED_WORK_GROUPS_GLOBAL_ROTATION() != null) {
            return ScheduleStrategy.FIXED_WORK_GROUPS_GLOBAL_ROTATION;
        }

        throw new NotImplementedException(node.getText());
    }

    private List<String> parseValues(CommentSt stmt, BlockContext blockContext, String semantics,
            List<ValueContext> values,
            TokenReportingService reportService) {

        List<String> result = new ArrayList<>();

        for (ValueContext value : values) {
            result.add(parseValue(stmt, blockContext, semantics, value, reportService));
        }

        return result;

    }

    private String parseValue(CommentSt stmt, BlockContext blockContext, String semantics, ValueContext value,
            TokenReportingService reportService) {
        if (value.NUMBER() != null) {
            String number = value.NUMBER().getText();

            double result = Double.parseDouble(number);
            if (result != (int) result) {
                throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR,
                        "Expected integer value, got " + result);
            }

            assert blockContext.getFunction() != null;
            String var = blockContext.getFunction().makeTemporary(semantics);

            blockContext.addInstruction(AssignmentInstruction.fromInteger(var, (int) result));

            return var;
        }
        if (value.identifier() != null) {
            String var = value.identifier().getText();

            if (blockContext.isGlobal(var)) {
                throw reportService.emitError(stmt, PassMessage.NOT_YET_IMPLEMENTED, "Global variable parameter");
            }
            String ssaName = blockContext.getCurrentName(var);
            if (ssaName == null) {
                throw reportService.emitError(stmt, PassMessage.MISSING_IDENTIFIER, "No such variable " + ssaName);
            }

            return ssaName;
        }

        throw new NotImplementedException(value.getText());
    }

    private static void log(String message) {
        if (ENABLE_DIAGNOSTICS) {
            System.out.print("[parallel_directive_identification] ");
            System.out.println(message);
        }
    }

}
