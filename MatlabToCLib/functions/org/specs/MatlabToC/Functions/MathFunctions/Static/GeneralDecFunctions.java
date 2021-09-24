package org.specs.MatlabToC.Functions.MathFunctions.Static;

import static org.specs.MatlabToC.Functions.MathFunctions.Static.GeneralDecTemplate.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

public class GeneralDecFunctions extends CirBuilder {

    // private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    /**
     * @param data
     */
    public GeneralDecFunctions(DataStore setup) {
        super(setup);
    }

    /**
     * Implementation for the Matlab built-in function 'linspace' when the argument 'n' is not specified or when it is a
     * constant. In both cases the array 'vec' created inside the implementation can be declared statically.
     * 
     * @param n
     * @return
     */
    public FunctionInstance newLinspaceDec(int numDivisions) {

        String cfunctionName = "linspace_dec_" + numDivisions;

        String cBody = null;
        if (numDivisions == 1) {
            cBody = SpecsIo.getResource(GeneralDecResource.LINSPACE_DEC_EQUAL_1.getResource());
        } else {
            cBody = SpecsIo.getResource(GeneralDecResource.LINSPACE_DEC_GREATER_1.getResource());
        }

        int n_steps = numDivisions - 1;
        cBody = GeneralDecTemplate.parseTemplate(cBody, N_STEPS.getTag(), Integer.toString(n_steps));

        // Input variables
        List<String> inputNames = SpecsFactory.newArrayList();
        inputNames.add(GeneralDecTemplate.VAR_NAME_END);

        // VariableType doubleType = VariableTypeFactoryOld.newNumeric(NumericType.Double);
        VariableType doubleType = getNumerics().newDouble();
        List<VariableType> inputTypes = SpecsFactory.newArrayList();
        inputTypes.add(doubleType);

        // If 'n' != 1 we will use the 'start' argument too
        if (numDivisions != 1) {
            inputNames.add(0, GeneralDecTemplate.VAR_NAME_START);
            inputTypes.add(doubleType);
        }

        // Output variable
        String outputName = GeneralDecTemplate.VAR_NAME_OUTPUT;
        VariableType outputType = StaticMatrixType.newInstance(doubleType, Arrays.asList(1, numDivisions));

        // Function types
        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
                outputType);

        String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();

        LiteralInstance instance = new LiteralInstance(functionTypes, cfunctionName, cFilename, cBody);

        // If 'n' != 1, needs math.h
        if (numDivisions != 1) {
            instance.setCustomImplementationIncludes(SystemInclude.Math.getIncludeName());
        }

        return instance;
    }
}
