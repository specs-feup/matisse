package org.specs.MatlabToC.Functions.StringFunctions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.Utilities.MatisseChecker;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Deals with strsplit(mat, pat), where pat is a constant string, consisting of a single char with no special regex
 * meaning.
 * 
 * @author Luís Reis
 *
 */
public class StrsplitConstantCharBuilder implements InstanceProvider {

    private static final String FILE_NAME = "lib/matisse_string";

    private StrsplitConstantCharBuilder() {
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        MatrixType inputType = data.getInputType(MatrixType.class, 0);
        ScalarType elementType = inputType.matrix().getElementType();
        MatrixType underlyingType = DynamicMatrixType.newInstance(elementType, TypeShape.newRow());
        DynamicCellType outputType = new DynamicCellType(underlyingType, TypeShape.newRow());

        FunctionType functionType = FunctionTypeBuilder
                .newWithSingleOutputAsInput()
                .addInput("in", inputType)
                .addInput("pattern_string", data.getInputType(StringType.class, 1))
                .addOutputAsInput("out", outputType)
                .build();

        CNode inNode = CNodeFactory.newVariable("in", inputType);
        CNode iNode = CNodeFactory.newVariable("i", data.getNumerics().newInt());
        CNode outNode = CNodeFactory.newVariable("out", outputType.pointer().getType(true));
        CNode numChunksNode = CNodeFactory.newVariable("num_chunks", data.getNumerics().newInt());

        String body = SpecsIo.getResource(StringResources.STRSPLIT);

        FunctionCallNode inLength = FunctionInstanceUtils.getFunctionCall(
                inputType.functions().numel(),
                data,
                Arrays.asList(inNode));
        body = body.replaceAll("<IN_LENGTH>", inLength.getCode());

        FunctionCallNode inDataI = FunctionInstanceUtils.getFunctionCall(
                inputType.functions().get(),
                data,
                Arrays.asList(inNode, iNode));
        body = body.replaceAll("<IN_DATA_i>", inDataI.getCode());

        FunctionCallNode inDataJ = FunctionInstanceUtils.getFunctionCall(
                inputType.functions().get(),
                data,
                Arrays.asList(inNode, CNodeFactory.newVariable("j", data.getNumerics().newInt())));
        body = body.replaceAll("<IN_DATA_j>", inDataJ.getCode());

        FunctionCallNode allocOut = FunctionInstanceUtils.getFunctionCall(
                outputType.cell().functions().createFromDims(),
                data,
                Arrays.asList(CNodeFactory.newCNumber(1), numChunksNode),
                Arrays.asList(outNode));
        body = body.replaceAll("<ALLOC_OUT>", allocOut.getCode());

        String chunkDeclaration = underlyingType.code().getDeclarationWithInputs("chunk", null);
        body = body.replaceAll("<DECLARE_CHUNK_MATRIX>", chunkDeclaration);

        FunctionCallNode allocChunk = FunctionInstanceUtils.getFunctionCall(underlyingType.functions().create(),
                data,
                Arrays.asList(CNodeFactory.newLiteral("end - start != 0", data.getNumerics().newInt()),
                        CNodeFactory.newLiteral("end - start", data.getNumerics().newInt())),
                Arrays.asList(CNodeFactory.newVariable("chunk", underlyingType)));
        body = body.replaceAll("<ALLOC_CHUNK>", allocChunk.getCode());

        LiteralInstance instance = new LiteralInstance(functionType,
                "strsplit_by_char_" + inputType.getSmallId() + "_1",
                FILE_NAME,
                body);

        instance.setCustomImplementationIncludes(SystemInclude.Stdio, SystemInclude.Stdlib, SystemInclude.Stdint);

        instance.addInstance(inLength.getFunctionInstance());
        instance.addInstance(inDataI.getFunctionInstance());
        instance.addInstance(allocOut.getFunctionInstance());
        instance.addInstance(allocChunk.getFunctionInstance());

        return instance;
    }

    public static InstanceProvider getProvider() {
        // TODO: Check that the first input is a matrix of characters?

        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(2)
                .numOfOutputsAtMost(1)
                .is1DMatrix(0)
                .isString(1)
                .addCheck(data -> isAcceptablePattern(data.getInputType(StringType.class, 1)));

        return new GenericInstanceProvider(checker, new StrsplitConstantCharBuilder());
    }

    private static final List<Character> SPECIAL_REGEX_CHARS = Arrays.asList(
            '\\', '^', '$', '.', '|', '?', '*', '+', '(', ')', '[', ']', '{');

    private static boolean isAcceptablePattern(StringType inputType) {
        String str = inputType.getString();
        if (str == null || str.length() != 1) {
            return false;
        }

        char ch = str.charAt(0);
        return !SPECIAL_REGEX_CHARS.contains(ch);
    }

}
