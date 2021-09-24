package org.specs.CIRFunctions.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;

public class CreateAndCopyMatrixInstanceProvider implements InstanceProvider {

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        MatrixType inType = data.getInputType(MatrixType.class, 0);
        MatrixType outType = (MatrixType) data.getOutputType();

        FunctionType functionType = FunctionTypeBuilder.newWithSingleOutputAsInput()
                .addInput("in", inType)
                .addOutputAsInput("out", outType)
                .build();

        final boolean shouldValidate = inType.usesDynamicAllocation();

        MatrixType leftMatrixType = (MatrixType) outType;

        final InstanceProvider createProvider = leftMatrixType.functions().createFromMatrix();
        final InstanceProvider copyProvider = leftMatrixType.functions().copy();

        ProviderData createCallData = data.create(inType);
        createCallData.setOutputType(outType);
        FunctionInstance createInstance = null;
        if (leftMatrixType.usesDynamicAllocation()) {
            createInstance = createProvider.getCheckedInstance(createCallData);
            assert createInstance != null;
        }

        FunctionInstance copyInstance = copyProvider.getCheckedInstance(data.create(inType, outType));

        InlineCode code = tokens -> {
            CNode input = tokens.get(0);
            CNode output = tokens.get(1);

            final List<CNode> inputs = Arrays.asList(input);
            final List<CNode> outputs = Arrays.asList(output);

            final List<CNode> copyNodes = new ArrayList<>();

            if (leftMatrixType.usesDynamicAllocation()) {
                copyNodes.add(FunctionInstanceUtils
                        .getFunctionCall(createProvider, data, inputs, outputs));
            }
            copyNodes.add(FunctionInstanceUtils
                    .getFunctionCall(copyProvider, data, Arrays.asList(input, output)));

            if (shouldValidate) {
                ProviderData notNullData = data.createFromNodes(input);
                CNode notNullNode = IsDefinedProvider
                        .create()
                        .getCheckedInstance(notNullData)
                        .newFunctionCall(input);

                return IfNodes.newIfThen(notNullNode, copyNodes).getCode();
            } else {
                if (copyNodes.size() == 1) {
                    return copyNodes.get(0).getCode();
                }
                return copyNodes
                        .stream()
                        .map(CNode::getCode)
                        .map(stmt -> stmt + ";")
                        .collect(Collectors.joining("\n"));
            }

        };
        InlinedInstance instance = new InlinedInstance(functionType, "$create_and_copy$" + inType.getSmallId(), code);
        Set<FunctionInstance> dependentInstances = new HashSet<>();
        if (createInstance != null) {
            dependentInstances.add(createInstance);
            dependentInstances.addAll(createInstance.getCallInstances());
        }
        dependentInstances.add(copyInstance);
        dependentInstances.addAll(copyInstance.getCallInstances());
        instance.setCallInstances(dependentInstances);
        return instance;
    }

}
