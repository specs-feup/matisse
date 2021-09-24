package org.specs.CIRFunctions.LibraryFunctions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunction;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunctionUtils;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Void.VoidType;

/**
 * C Functions in to 'stdlib' library.
 * 
 * @author Joao Bispo
 * 
 */
public enum CStdlibFunction implements CLibraryFunction {

    ABS("abs") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newInt();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newInt());
	}

    },

    ATOI("atoi") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newInt();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return null;
	}

    },

    FREE("free") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return VoidType.newInstance();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return null;
	}
    },

    EXIT("exit") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return VoidType.newInstance();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newInt());
	}
    };

    /**
     * Instance variables
     */
    private final String functionName;

    private CStdlibFunction(String functionName) {
	this.functionName = functionName;
    }

    @Override
    public String getFunctionName() {
	return functionName;
    }

    @Override
    public SystemInclude getLibrary() {
	return SystemInclude.Stdlib;
    }

    @Override
    public abstract List<VariableType> getInputTypes(NumericFactory numerics);

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	return CLibraryFunctionUtils.newInstance(this, data);
    }

}
