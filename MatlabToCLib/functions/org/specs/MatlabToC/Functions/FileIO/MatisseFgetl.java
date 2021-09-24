package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.SpecsIo;

public class MatisseFgetl implements InstanceProvider {
    public static InstanceProvider getProvider() {
        MatisseChecker checker = new MatisseChecker()
                .numOfOutputs(2)
                .numOfInputs(1)
                .isInteger(0);

        return new GenericInstanceProvider(checker, new MatisseFgetl());
    }

    private MatisseFgetl() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        VariableType charType = data.getNumerics().newChar();
        MatrixType outputType = DynamicMatrixType.newInstance(charType, TypeShape.newRow());

        FunctionType functionType = FunctionTypeBuilder
                .newWithOutputsAsInputs()
                .addInput("res", data.getNumerics().newInt())
                .addOutputAsInput("str", outputType)
                .addOutputAsInput("eof", data.getNumerics().newInt())
                .withSideEffects()
                .build();

        String body = SpecsIo.getResource(FileResources.MATISSE_FGETL);

        ProviderData createData = data.create(data.getNumerics().newInt(1), data.getNumerics().newInt());
        createData.setOutputType(outputType.matrix().getElementType());
        FunctionInstance createInstance = outputType.functions().create().newCInstance(createData);
        CNode callNode = createInstance.newFunctionCall(CNodeFactory.newCNumber(1),
                CNodeFactory.newLiteral("len", data.getNumerics().newInt(), PrecedenceLevel.Atom),
                CNodeFactory.newVariable("str", outputType.pointer().getType(true)));

        body = body.replaceAll("<ALLOC>", callNode.getCode() + ";");

        LiteralInstance instance = new LiteralInstance(functionType, "matisse_fgetl", FopenProvider.FILE, body);
        instance.setCustomImplementationIncludes(SystemInclude.String);
        instance.addInstance(InitializeFileResourcesProvider.newInstance(data.create()));
        instance.addInstance(createInstance);

        return instance;
    }
}
