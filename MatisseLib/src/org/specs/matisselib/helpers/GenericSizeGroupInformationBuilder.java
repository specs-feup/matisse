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

package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.ProjectPassServices;
import org.specs.matisselib.helpers.sizeinfo.BranchInstructionBuilder;
import org.specs.matisselib.helpers.sizeinfo.ForInstructionBuilder;
import org.specs.matisselib.helpers.sizeinfo.InstructionInformationBuilder;
import org.specs.matisselib.helpers.sizeinfo.IterInformationBuilder;
import org.specs.matisselib.helpers.sizeinfo.SizeGroupInformation;
import org.specs.matisselib.helpers.sizeinfo.SizeInfoBuilderContext;
import org.specs.matisselib.services.AdditionalInformationBuildersService;
import org.specs.matisselib.services.ScalarValueInformationBuilderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.ssa.instructions.SsaInstruction;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class GenericSizeGroupInformationBuilder {

    private static final boolean ENABLE_LOGGING = false;
    private static final List<InstructionInformationBuilder> DEFAULT_BUILDERS = new ArrayList<>();

    static {
        DEFAULT_BUILDERS.add(new IterInformationBuilder());
        DEFAULT_BUILDERS.add(new ForInstructionBuilder());
        DEFAULT_BUILDERS.add(new BranchInstructionBuilder());
    }

    public static SizeGroupInformation build(List<InstructionInformationBuilder> additionalBuilders,
            FunctionBody body,
            ScalarValueInformationBuilderService scalarValueBuilder,
            Function<String, Optional<VariableType>> typeGetter) {

        return build(additionalBuilders, body, scalarValueBuilder, typeGetter, GenericSizeGroupInformationBuilder::log);
    }

    public static SizeGroupInformation build(List<InstructionInformationBuilder> additionalBuilders,
            FunctionBody body,
            ScalarValueInformationBuilderService scalarValueBuilder,
            Function<String, Optional<VariableType>> typeGetter,
            Consumer<String> logger) {

        log("Function: " + body.getName());

        SizeGroupInformation info = new SizeGroupInformation(typeGetter, scalarValueBuilder,
                logger);

        List<InstructionInformationBuilder> builders = new ArrayList<>(DEFAULT_BUILDERS);
        builders.addAll(additionalBuilders);

        return buildInfoFor(builders, body, 0, typeGetter, null, info);
    }

    private static class SimpleSizeInfoBuilderContext implements SizeInfoBuilderContext {

        private final FunctionBody body;
        private final String loopSize;
        private final SizeGroupInformation info;
        private final Function<String, Optional<VariableType>> typeGetter;
        private final int blockId;
        private final List<InstructionInformationBuilder> builders;

        SimpleSizeInfoBuilderContext(List<InstructionInformationBuilder> builders,
                FunctionBody body,
                int blockId,
                String loopSize,
                Function<String, Optional<VariableType>> typeGetter,
                SizeGroupInformation info) {

            this.builders = builders;
            this.blockId = blockId;
            this.loopSize = loopSize;
            this.body = body;
            this.typeGetter = typeGetter;
            this.info = info;
        }

        @Override
        public void log(String msg) {
            GenericSizeGroupInformationBuilder.log(msg);
        }

        @Override
        public FunctionBody getFunctionBody() {
            return body;
        }

        @Override
        public int getBlockId() {
            return blockId;
        }

        @Override
        public String getLoopSize() {
            return loopSize;
        }

        @Override
        public SizeGroupInformation getCurrentInfo() {
            return info;
        }

        @Override
        public SizeGroupInformation buildInfoFor(int blockId,
                String loopSize,
                SizeGroupInformation info) {

            return GenericSizeGroupInformationBuilder.buildInfoFor(getBuilders(), body, blockId, typeGetter, loopSize,
                    info);
        }

        @Override
        public Optional<VariableType> getVariableType(String name) {
            return typeGetter.apply(name);
        }

        @Override
        public SizeGroupInformation handleInstruction(SizeInfoBuilderContext ctx,
                SizeGroupInformation info,
                int blockId,
                String end,
                SsaInstruction instruction) {

            return GenericSizeGroupInformationBuilder.handleInstruction(getBuilders(), body, typeGetter, info, blockId,
                    end,
                    instruction);
        }

        @Override
        public List<InstructionInformationBuilder> getBuilders() {
            return builders;
        }
    }

    private static SizeGroupInformation buildInfoFor(List<InstructionInformationBuilder> builders,
            FunctionBody body,
            int blockId,
            Function<String, Optional<VariableType>> typeGetter,
            String loopSize,
            SizeGroupInformation info) {

        SsaBlock block = body.getBlock(blockId);
        for (SsaInstruction instruction : block.getInstructions()) {
            info = handleInstruction(builders, body, typeGetter, info, blockId, loopSize, instruction);
        }

        return info;

    }

    @SuppressWarnings("resource")
    private static SizeGroupInformation handleInstruction(List<InstructionInformationBuilder> builders,
            FunctionBody body,
            Function<String, Optional<VariableType>> typeGetter,
            SizeGroupInformation info,
            int blockId,
            String loopSize,
            SsaInstruction instruction) {

        SizeInfoBuilderContext ctx = new SimpleSizeInfoBuilderContext(builders, body, blockId, loopSize, typeGetter,
                info);

        for (InstructionInformationBuilder builder : ctx.getBuilders()) {
            if (builder.accepts(instruction)) {
                return builder.apply(ctx, instruction);
            }
        }

        if (instruction.getOwnedBlocks().size() != 0) {
            log("Unrecognized block type: " + instruction);
        } else {
            info.addInstructionInformation(instruction);
        }

        return info;
    }

    private static void log(String message) {
        if (GenericSizeGroupInformationBuilder.ENABLE_LOGGING) {
            System.out.print("[generic_size_group_information] ");
            System.out.println(message);
        }
    }

    public static SizeGroupInformation build(FunctionBody body, DataStore passData,
            Function<String, Optional<VariableType>> typeGetter) {

        AdditionalInformationBuildersService additionalInformationBuildersService = passData
                .get(ProjectPassServices.ADDITIONAL_INFORMATION_BUILDERS);
        return build(additionalInformationBuildersService.getBuilders(),
                body,
                passData.get(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER),
                typeGetter);
    }
}
