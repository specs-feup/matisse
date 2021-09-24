package org.specs.CIR.Utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;

public class AssignmentUtils {
    public static CNode buildAssignmentNode(CNode leftHand, CNode rightHand, ProviderData providerData) {
        VariableType leftType = leftHand.getVariableType();

        CNode node;
        if (leftType.canBeAssignmentCopied()) {
            node = CNodeFactory.newAssignment(leftHand, rightHand);
        } else {
            InstanceProvider assignProvider = leftType.functions().assign();

            final List<CNode> inputs = Arrays.asList(rightHand);
            ProviderData data = providerData.create(rightHand.getVariableType());
            data.setOutputType(leftHand.getVariableType());
            FunctionInstance instance = assignProvider.newCInstance(data);

            FunctionCallNode callNode = instance.newFunctionCall(inputs);
            if (instance.getFunctionType().getOutputAsInputNames().isEmpty()) {
                node = CNodeFactory.newAssignment(leftHand, callNode);
            } else {
                callNode.getFunctionInputs().setInput(1, leftHand);
                node = callNode;
            }
        }

        return node;
    }

    public static Set<FunctionInstance> getAssignmentInstances(VariableType leftType, VariableType rightType,
            ProviderData providerData) {
        if (leftType.canBeAssignmentCopied()) {
            return Collections.emptySet();
        }

        InstanceProvider assignProvider = leftType.functions().assign();
        ProviderData data = providerData.create(rightType);
        data.setOutputType(leftType);
        FunctionInstance instance = assignProvider.newCInstance(data);

        Set<FunctionInstance> set = new HashSet<>();
        set.add(instance);
        return set;
    }
}
