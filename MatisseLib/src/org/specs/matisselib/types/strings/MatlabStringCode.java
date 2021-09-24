package org.specs.matisselib.types.strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.Views.Code.ACode;
import org.specs.matisselib.functions.strings.MakeEmptyMatlabStringInstance;

public class MatlabStringCode extends ACode {

    public MatlabStringCode(MatlabStringType type) {
        super(type);
    }

    @Override
    public String getSimpleType() {
        return MatlabStringStructInstance.STRUCT_NAME + "*";
    }

    @Override
    public CInstructionList getSafeDefaultDeclaration(CNode node, ProviderData providerData) {
        CInstructionList instructions = new CInstructionList();

        instructions.addAssignment(node, CNodeFactory.newLiteral("0", MatlabStringType.STRING_TYPE));
        FunctionCallNode makeEmptyStringNode = FunctionInstanceUtils.getFunctionCall(
                MakeEmptyMatlabStringInstance.getProvider(),
                providerData,
                Collections.emptyList(),
                Arrays.asList(node));
        if (makeEmptyStringNode.getFunctionInstance().getFunctionType().getOutputAsInputTypes().isEmpty()) {
            instructions.addAssignment(node, makeEmptyStringNode);
        } else {
            instructions.addInstruction(makeEmptyStringNode);
        }

        return instructions;
    }

    @Override
    public boolean requiresExplicitInitialization() {
        return true;
    }

    @Override
    public String getDeclarationWithInputs(String variableName, List<String> values) {
        return getSimpleType() + " " + variableName + " = 0";
    }

    @Override
    public Set<FunctionInstance> getInstances() {
        Set<FunctionInstance> instances = new HashSet<>();
        instances.add(new MatlabStringStructInstance());

        return instances;
    }

    @Override
    public Set<String> getIncludes() {
        return new MatlabStringStructInstance().getCallIncludes();
    }
}
