package org.specs.MatlabToCTester.Outputs;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.List;

import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Types.VariableType;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.CCode.CWriter;

import com.jmatio.io.MLInt32;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLUInt16;
import com.jmatio.types.MLUInt32;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Represents a variable read from the output of a binary execution.
 * </p>
 * The value of the variable is always stored as a list of {@link Number}, both when the variable represents an
 * n-dimensional matrix or a scalar.
 * </p>
 * 
 * If this is representing a matrix then the list of double will be the <b>column-major linearized</b> version of that
 * matrix.
 * 
 * @author Pedro Pinto
 * 
 */
public class NumericOutputVariable extends OutputVariable {

    private final List<Number> variableValues;
    private final List<Integer> dimensions;
    private final VariableType numericType;
    private final MemoryLayout memLayout;

    /**
     * Returns a new instance {@link NumericOutputVariable}. Assumes that the variable is of the Matlab type 'Double'.
     * 
     * @param name
     *            - the name of the variable
     * @param variableValues
     *            - the variable values
     * @param dimensions
     *            - the shape of this variable
     */
    /*
    public NumericOutputVariable(String name, List<Number> variableValues, List<Integer> dimensions) {
    
    // Uses double as the default numeric type
    this(name, variableValues, dimensions, NumericClassName.DOUBLE);
    }
    */

    /**
     * Returns a new instance {@link NumericOutputVariable}.
     * 
     * @param name
     *            - the name of the variable
     * @param variableValues
     *            - the variable values
     * @param dimensions
     *            - the shape of this variable
     * @param numericType
     *            - the numeric type of the variable
     * @param memLayout
     */
    public NumericOutputVariable(String name, List<Number> variableValues, List<Integer> dimensions,
            VariableType numericType, MemoryLayout memLayout) {

        super(name);

        checkArgument(name != null, "name must not be null");
        checkArgument(variableValues != null, "variableValues must not be null");
        checkArgument(dimensions != null, "dimensions must not be null");
        checkArgument(numericType != null, "numericType must not be null");
        checkArgument(memLayout != null, "memLayout must not be null");

        this.variableValues = variableValues;
        this.dimensions = dimensions;
        this.numericType = numericType;
        this.memLayout = memLayout;
    }

    public List<Number> getVariableValues() {
        return variableValues;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public boolean isScalar() {
        if (dimensions.size() > 1) {
            return false;
        }

        if (dimensions.get(0) != 1) {
            return false;
        }

        return true;
    }

    /*
        public int getNumberDimensions() {
    	// If has one dimension but
    	return dimensions.size();
        }
    */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("\n\t\t");
        builder.append(name);
        builder.append(dimensions.toString());
        builder.append("=");
        builder.append(variableValues.toString());
        builder.append("\n");

        return builder.toString();
    }

    public int[] getDimensionsAsArray() {

        // Make a copy of dimensions
        List<Integer> dims = SpecsFactory.newArrayList(dimensions);

        // Correct for vector arrays
        if (dimensions.size() == 1 && !isScalar()) {
            dims.add(1);
        }

        // Convert to array
        int[] array = new int[dims.size()];

        for (int i = 0; i < dims.size(); i++) {

            array[i] = dims.get(i);
        }

        return array;
        /*
        int[] array = new int[dimensions.size()];
        
        for (int i = 0; i < dimensions.size(); i++) {
        
        array[i] = dimensions.get(i);
        }
        
        return array;
        */
    }

    @Override
    public MLArray toMLArray() {

        NumericClassName type = MatlabToCTypesUtils.getNumericClass(numericType);
        MLArray variable = null;

        // If it is a scalar 'convert' it to a 1x1 matrix
        if (isScalar()) {
            // if (getNumberDimensions() == 1) {

            int[] scalarMatrix = { 1, 1 };
            variable = newMLArray(name + "_C", scalarMatrix, type);

        } else {
            variable = newMLArray(name + "_C", getDimensionsAsArray(), type);
        }

        // Calculate cumulative product for subscript calculation
        List<Integer> cumprod = Collections.emptyList();
        if (memLayout == MemoryLayout.ROW_MAJOR) {
            cumprod = CWriter.newCumulativeProduct(variable);
        }

        // Set the values
        for (int i = 0; i < variableValues.size(); i++) {
            int matlabIndex = CWriter.indRow2IndCol(i, cumprod, variable.getDimensions(), memLayout);

            setMLValue(variable, variableValues.get(i), matlabIndex, type);
        }

        return variable;
    }

    /**
     * Sets the value of a MLArray according to a numeric type and the index.
     * 
     * @param variable
     * @param number
     * @param index
     * @param numericType
     */
    private static void setMLValue(MLArray variable, Number number, int index, NumericClassName numericType) {

        switch (numericType) {
        case INT8:
            ((MLInt8) variable).setReal(number.byteValue(), index);
            break;
        case UINT8:
            ((MLUInt8) variable).setReal(number.byteValue(), index);
            break;
        case INT16:
            ((MLInt16) variable).setReal(number.shortValue(), index);
            break;
        case UINT16:
            ((MLUInt16) variable).setReal(number.shortValue(), index);
            break;
        case INT32:
            ((MLInt32) variable).setReal(number.intValue(), index);
            break;
        case UINT32:
            ((MLUInt32) variable).setReal(number.intValue(), index);
            break;
        case INT64:
            ((MLInt64) variable).setReal(number.longValue(), index);
            break;
        case UINT64:
            ((MLUInt64) variable).setReal(number.longValue(), index);
            break;
        case DOUBLE:
            ((MLDouble) variable).setReal(number.doubleValue(), index);
            break;
        case SINGLE:
            ((MLSingle) variable).setReal(number.floatValue(), index);
            break;
        case CHAR:
            ((MLChar) variable).setChar((char) number.intValue(), index);
            break;
        /*
        case INT:
        ((MLInt32) variable).setReal(number.intValue(), index);
        break;
        */
        default:
            throw new RuntimeException("Case not supported: '" + numericType + "'");
        }
    }

    /**
     * Creates a new MLArray according to the numeric type.
     * 
     * @param string
     * @param dimensionsAsArray
     * @param numericType
     * @return
     */
    private static MLArray newMLArray(String string, int[] dimensionsAsArray, NumericClassName numericType) {

        switch (numericType) {
        case INT8:
            return new MLInt8(string, dimensionsAsArray);
        case UINT8:
            return new MLUInt8(string, dimensionsAsArray);
        case INT16:
            return new MLInt16(string, dimensionsAsArray);
        case UINT16:
            return new MLUInt16(string, dimensionsAsArray);
        case INT32:
            return new MLInt32(string, dimensionsAsArray);
        case UINT32:
            return new MLUInt32(string, dimensionsAsArray);
        case INT64:
            return new MLInt64(string, dimensionsAsArray);
        case UINT64:
            return new MLUInt64(string, dimensionsAsArray);
        case DOUBLE:
            return new MLDouble(string, dimensionsAsArray);
        case SINGLE:
            return new MLSingle(string, dimensionsAsArray);
        case CHAR:
            return new MLChar(string, dimensionsAsArray, MLArray.mxCHAR_CLASS, 0);
        /*
        case INT:
        return new MLInt32(string, dimensionsAsArray);
        */
        default:
            throw new RuntimeException("Case not supported: '" + numericType + "'");
        }
    }

}
