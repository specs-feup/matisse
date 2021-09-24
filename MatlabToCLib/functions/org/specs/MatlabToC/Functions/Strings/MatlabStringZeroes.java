package org.specs.MatlabToC.Functions.Strings;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.Functions.BaseFunctions.Static.ConstantArrayAllocBuilder;
import org.specs.matisselib.types.strings.MatlabStringType;

/**
 * The zeros() function normally would not return a string.<br/>
 * Unfortunately, functions like MATISSE_reserve_capacity (and who knows what else) use zeros and it's a hassle to
 * change them to return something else for just this case.<br/>
 * So, we'll take advantage of the fact that these functions define an explicit output type and define the zeros()
 * function for that case.
 * 
 * @author Luís Reis
 *
 */
public class MatlabStringZeroes implements InstanceProvider {

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
        if (!check(data)) {
            return Optional.empty();
        }

        return InstanceProvider.super.accepts(data);
    }

    private boolean check(ProviderData data) {
        if (data.getNargouts().orElse(1) != 1) {
            return false;
        }
        VariableType outputType = data.getOutputType();
        if (outputType == null) {
            return false;
        }
        if (!(outputType instanceof MatrixType)) {
            return false;
        }

        MatrixType matrixType = (MatrixType) outputType;
        ScalarType elementType = matrixType.matrix().getElementType();

        return elementType instanceof MatlabStringType;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {

        // FIXME: NULL is probably not acceptable. See if we can use empty string instead.
        CNode nullNode = CNodeFactory.newLiteral("NULL", MatlabStringType.STRING_TYPE, PrecedenceLevel.Atom);

        return new ConstantArrayAllocBuilder("zeros", nullNode).newCInstance(data);
    }

}
