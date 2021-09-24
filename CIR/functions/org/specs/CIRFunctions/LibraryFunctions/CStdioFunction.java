package org.specs.CIRFunctions.LibraryFunctions;

import java.util.List;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunction;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Void.VoidType;

/**
 * C Functions in to 'stdio' library.
 * 
 * @author Joao Bispo
 * 
 */
public enum CStdioFunction implements CLibraryFunction {

    PRINTF("printf") {

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return null;
	}

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return VoidType.newInstance();
	}

	@Override
	public boolean hasSideEffects() {
	    return true;
	}

    };

    private final String functionName;

    private CStdioFunction(String functionName) {
	this.functionName = functionName;
    }

    @Override
    public String getFunctionName() {
	return functionName;
    }

    @Override
    public SystemInclude getLibrary() {
	return SystemInclude.Stdio;
    }

}
