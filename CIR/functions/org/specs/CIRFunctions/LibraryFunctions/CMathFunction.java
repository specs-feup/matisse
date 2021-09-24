package org.specs.CIRFunctions.LibraryFunctions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRFunctions.LibraryFunctionsBase.CLibraryFunction;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

/**
 * C Functions in to 'math' library. (for example pow).
 * 
 * @author Pedro Pinto
 * 
 */
public enum CMathFunction implements CLibraryFunction {

    POW("pow") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble(), numerics.newDouble());
	}

    },

    POWF("powf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat(), numerics.newFloat());
	}

    },

    // TODO: New round function, that uses the math.h round, but casts result to integer
    ROUND("round") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    /**
     * Input float, output float.
     */
    ROUNDF("roundf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    /**
     * Input float, output long.
     */
    LROUNDF("lroundf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newType(CTypeV2.LONG);
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    /**
     * Input double, output long.
     */
    LROUND("lround") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newType(CTypeV2.LONG);
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    FABS("fabs") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    FABSF("fabsf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    EXP("exp") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    EXPF("expf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    FLOOR("floor") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    FLOORF("floorf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    CEIL("ceil") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Cint);
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    CEILF("ceilf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Cint);
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    ASIN("asin") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    ASINF("asinf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    SIN("sin") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    SINF("sinf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Float);
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    ACOS("acos") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    ACOSF("acos") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    COS("cos") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    COSF("cosf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Float);
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    LOG("log") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    LOGF("logf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Float);
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    SQRT("sqrt") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble());
	}

    },

    SQRTF("sqrtf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat());
	}

    },

    FMOD("fmod") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    // return VariableTypeFactoryOld.newNumeric(NumericType.Double);
	    return numerics.newDouble();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newDouble(), numerics.newDouble());
	}

    },

    FMODF("fmodf") {

	@Override
	public VariableType getOutputType(NumericFactory numerics) {
	    return numerics.newFloat();
	}

	@Override
	public List<VariableType> getInputTypes(NumericFactory numerics) {
	    return Arrays.asList(numerics.newFloat(), numerics.newFloat());
	}

    };

    /**
     * Instance variables
     */
    private final String functionName;

    /**
     * 
     * @param functionName
     * @param library
     * @param inputTypes
     * @param outputType
     */
    private CMathFunction(String functionName) {
	this.functionName = functionName;
    }

    @Override
    public String getFunctionName() {
	return functionName;
    }

    @Override
    public SystemInclude getLibrary() {
	return SystemInclude.Math;
    }

    @Override
    public abstract List<VariableType> getInputTypes(NumericFactory numerics);
}
