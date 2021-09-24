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

package org.specs.matisselib.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.functionproperties.DumpSsaProperty;
import org.specs.matisselib.functionproperties.ExportProperty;
import org.specs.matisselib.loopproperties.EstimatedIterationsProperty;
import org.specs.matisselib.loopproperties.InfusibleLoopProperty;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.ssa.BlockContext;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.instructions.AssumeMatrixIndicesInRangeDirectiveInstruction;
import org.specs.matisselib.ssa.instructions.AssumeMatrixSizesMatchDirectiveInstruction;
import org.specs.matisselib.ssa.instructions.DisableOptimizationDirectiveInstruction;
import org.specs.matisselib.ssa.instructions.LineInstruction;
import org.specs.matisselib.ssa.instructions.SpecializeScalarValueDirectiveInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class DirectiveParser {
    private static final Pattern SPECIALIZE_CONSTANT_PATTERN = Pattern
            .compile("^\\s*specialize\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s*$");

    private static final Pattern BY_REF_PATTERN = Pattern
            .compile("^\\s*by_ref\\s+(.*)$");

    private static final Pattern BY_REF_BODY_PATTERN = Pattern
            .compile("^([a-zA-Z][a-zA-Z0-9_]*\\s*(,\\s*[a-zA-Z][a-zA-Z0-9_]*\\s*)*)$");

    private static final Pattern DISABLE_OPTIMIZATION_PATTERN = Pattern
            .compile("^\\s*disable\\s+(.*)$");

    private static final Pattern EXPORT_PATTERN = Pattern.compile("^\\s*export(?:\\s*([a-zA-Z_][a-zA-Z0-9_]*))?\\s*$");

    public void parseDirective(CommentSt stmt, BlockContext blockContext,
            DataStore dataStore) {

        TokenReportingService reportService = dataStore.get(PassManager.NODE_REPORTING);

        String content = stmt.getCommentString();
        String directiveBody = content.substring(1).trim();// Remove leading '!'

        Matcher specializeConstantMatcher = SPECIALIZE_CONSTANT_PATTERN
                .matcher(directiveBody);
        Matcher byRefMatcher = BY_REF_PATTERN
                .matcher(directiveBody);
        Matcher disableOptimizationMatcher = DISABLE_OPTIMIZATION_PATTERN
                .matcher(directiveBody);
        Matcher exportMatcher = EXPORT_PATTERN.matcher(directiveBody);
        if (specializeConstantMatcher.matches()) {
            if (blockContext.getBlockId() != 0) {
                throw new RuntimeException("Found %!specialize directive outside of function header");
            }

            String varName = specializeConstantMatcher.group(1);

            SpecializeScalarValueDirectiveInstruction directive = new SpecializeScalarValueDirectiveInstruction(
                    varName);

            blockContext.prependInstruction(directive);
            blockContext.prependInstruction(new LineInstruction(stmt.getLine()));
        } else if (byRefMatcher.matches()) {
            String byRefBody = byRefMatcher.group(1).trim();

            if (BY_REF_BODY_PATTERN.matcher(byRefBody).matches()) {

                String[] variables = byRefBody.split(",");
                for (String variable : variables) {
                    addByRef(blockContext.getFunction(), stmt, variable.trim(), reportService);
                }
            } else {
                reportService.emitMessage(stmt, PassMessage.UNRECOGNIZED_DIRECTIVE,
                        "Invalid format for directive: " + content);
            }
        } else if (directiveBody
                .equals(AssumeMatrixIndicesInRangeDirectiveInstruction.DIRECTIVE_CONTENT)) {

            blockContext.prependInstruction(new AssumeMatrixIndicesInRangeDirectiveInstruction());
            blockContext.prependInstruction(new LineInstruction(stmt.getLine()));

        } else if (directiveBody
                .equals(AssumeMatrixSizesMatchDirectiveInstruction.DIRECTIVE_CONTENT)) {

            blockContext.prependInstruction(new AssumeMatrixSizesMatchDirectiveInstruction());
            blockContext.prependInstruction(new LineInstruction(stmt.getLine()));
        } else if (disableOptimizationMatcher.matches()) {
            String optimizationsToDisable = disableOptimizationMatcher.group(1).trim();
            for (String field : optimizationsToDisable.split(",")) {
                String optimization = field.trim();
                if (optimization.matches("^[a-zA-Z0-9-_]+$")) {
                    blockContext.prependInstruction(new DisableOptimizationDirectiveInstruction(optimization));
                }
            }
        } else if (exportMatcher.matches()) {
            String abiName = exportMatcher.group(1);
            // abiName may be null
            FunctionBody body = blockContext.getFunction();
            if (body.hasProperty(ExportProperty.class)) {
                throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR, "Duplicated %!export directive.");
            }

            body.addProperty(new ExportProperty(abiName));
        } else if (directiveBody.equals("dump_ssa")) {
            FunctionBody body = blockContext.getFunction();
            if (body.hasProperty(DumpSsaProperty.class)) {
                throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR, "Duplicated %!dump_ssa directive.");
            }

            body.addProperty(new DumpSsaProperty());
        } else if (directiveBody.equals("infusible")) {
            blockContext.addLoopProperty(new InfusibleLoopProperty());
        } else if (directiveBody.matches("^estimated_iterations\\s+.*$")) {
            String iterationsStr = directiveBody.substring("estimated_iterations".length()).trim();
            if (!iterationsStr.matches("^[0-9]+$")) {
                throw reportService.emitError(stmt, PassMessage.CORRECTNESS_ERROR,
                        "%!estimated_iterations requires an integer iteration count, got '" + iterationsStr + "'");
            }
            int numIters = Integer.parseInt(iterationsStr, 10);
            blockContext.addLoopProperty(new EstimatedIterationsProperty(numIters));
        } else {
            reportService.emitMessage(stmt, PassMessage.UNRECOGNIZED_DIRECTIVE,
                    "The directive " + content + " is not recognized.");
        }
    }

    private void addByRef(FunctionBody source, StatementNode stmt, String variableName,
            TokenReportingService reportService) {

        if (source.indexOfInputName(variableName) < 0) {
            throw reportService.emitError(
                    stmt,
                    PassMessage.MISSING_IDENTIFIER,
                    "%!by_ref argument '" + variableName + "' does not name a function input.");
        }

        if (source.indexOfOutputName(variableName) < 0) {
            throw reportService.emitError(
                    stmt,
                    PassMessage.MISSING_IDENTIFIER,
                    "%!by_ref argument '" + variableName + "' does not name a function output.");
        }

        if (source.isByRef(variableName)) {
            throw reportService.emitError(
                    stmt,
                    PassMessage.CORRECTNESS_ERROR,
                    "Variable '" + variableName + "' was already previously declared as being by reference.");
        }

        source.addByRef(variableName);
    }
}
