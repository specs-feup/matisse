package org.specs.MatlabAspects;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.matisselib.types.DynamicCellType;

import pt.up.fe.specs.util.SpecsLogs;

public class Symbol {
    public static int NONE = 0;
    public static int MULTI = 1;
    public static int HXW = 2;
    /**
     * The number of dimensions is known, but not the size of each dimension
     */
    // public static int UNBOUNDED_ARRAY = 3;
    /**
     * The number of dimensions and the size of each dimension is known
     */
    public static int ARRAY_SHAPE = 4;
    /**
     * It is known that it is an array, although not the dimensions nor the size of each dimension
     */
    public static int UNDEFINED_ARRAY = 5;
    public static int DIM_STAR = -1;

    public boolean definer;
    public String name; // nome da variavel

    public String matlabType; // se e' uint8 int32, etc.

    // Indicates the type according to the Integer values defined in the
    // beginning of the file
    public int dimType;
    public int quantizerObject = -1;

    // so sao utilizados no caso de ser HxW:
    public int dimH = -2;
    public int dimW = -2;

    // private List<Integer> shape;
    private TypeShape matrixShape;

    private TypeShape cellShape;

    /**
     * The number of dimensions of the type. 0 is a scalar value, 1 is one-dimensional array, etc...
     * <p>
     * Used by: UNBOUNDED_ARRAY
     */
    // public int numDim = 0;

    /**
     * 
     * @param name
     */
    public Symbol(String name) {
        this(name, "", Symbol.NONE);
        // this.name = name;
    }

    public Symbol(String name, String matlabType, int dimType) {
        this.name = name;
        this.matlabType = matlabType;
        this.dimType = dimType;
        this.definer = false;

        // shape = new ArrayList<Integer>();
        // matrixShape = TypeShape.newUndefinedShape();
        this.matrixShape = null;
        this.cellShape = null;
    }

    /**
     * @return the matrixShape
     */
    public TypeShape getMatrixShape() {
        return this.matrixShape;
    }

    public TypeShape getCellShape() {
        return this.cellShape;
    }

    /**
     * @param matrixShape
     *            the matrixShape to set
     */
    public void setMatrixShape(TypeShape matrixShape) {
        this.matrixShape = matrixShape;
    }

    public void setCellShape(TypeShape cellShape) {
        this.cellShape = cellShape;
    }

    /*
    public void addDimToShape(int dimSize) {
    shape.add(dimSize);
    }
    */

    public void setDefine(boolean def) {
        this.definer = def;
    }

    @Override
    public String toString() {

        String ret = "";
        if (this.definer == true) {
            ret = "Definer: ";
        }
        ret += this.name + "  ";
        if (this.dimType == Symbol.MULTI) {
            ret += "multi ";
        } else if (this.dimType == Symbol.HXW) {
            String aux1 = "" + this.dimH;
            String aux2 = "" + this.dimW;
            if (this.dimH == Symbol.DIM_STAR) {
                aux1 = "*";
            }
            if (this.dimW == Symbol.DIM_STAR) {
                aux2 = "*";
            }
            ret += aux1 + "x" + aux2 + " ";
        }
        ret += this.matlabType;
        if (this.matlabType.equals("fixed") || this.matlabType.equals("ufixed")) {
            ret += " props: " + this.quantizerObject;
        }

        return ret;
    }

    public String toStringInfo() {
        StringBuilder builder = new StringBuilder();

        builder.append("Var Name:" + this.name).append("\n");
        builder.append("DimH:" + this.dimH).append("\n");
        builder.append("DimW:" + this.dimW).append("\n");
        builder.append("DimType:" + this.dimType).append("\n");
        builder.append("MatlabType:" + this.matlabType).append("\n");
        builder.append("Quantizer:" + this.quantizerObject).append("\n");

        return builder.toString();
    }

    public static int getTypeValue(String type) {
        if (type.startsWith("int")) {
            type = type.substring(3);
            return Integer.parseInt(type);
        } else if (type.equals("double")) {
            return 64;
        } else if (type.equals("single") || type.equals("float")) {
            return 32;
        } else if (type.endsWith("fixed")) {
            return 64;
        }
        return -1;
    }

    public void copySymbol(Symbol s) {
        this.matlabType = s.matlabType;
        this.dimType = s.dimType;
        this.quantizerObject = s.quantizerObject;
    }

    public VariableType convert(TypeDecoder decoder) {

        // Get base type
        String symbolType = this.matlabType;

        // For compatibility with previous version
        if (symbolType.equals("single")) {
            symbolType = "float";
        }

        // Check if can be decoded in a VariableType
        // System.out.println("SHAPE BEFORE:" + matrixShape);
        VariableType varType = decoder.decode(symbolType);
        // System.out.println("SHAPE AFTER:" + matrixShape);
        // Check if corresponds to a simply NumericType
        if (varType == null) {
            SpecsLogs.warn("String representation of typeA not supported:" + symbolType);
            SpecsLogs.warn(toStringInfo());
            return null;
        }

        if (this.matrixShape != null) {
            // If shape is defined, return a static matrix type
            if (this.matrixShape.isFullyDefined()) {
                // if (dimType == Symbol.ARRAY_SHAPE) {
                varType = StaticMatrixType.newInstance(varType, getMatrixShape().getDims());
            } else {
                varType = DynamicMatrixType.newInstance(varType, getMatrixShape());
            }

        }

        if (this.cellShape != null) {
            varType = new DynamicCellType(varType, getCellShape());
        }

        return varType;
    }
}
