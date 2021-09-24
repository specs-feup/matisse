package org.specs.matisselib.types.strings;

import java.util.Arrays;
import java.util.Collection;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;

public class MatlabStringTypeDecoder implements TypeDecoder {

    @Override
    public VariableType decode(String typeString) {
        if (typeString.equals("string")) {
            return MatlabStringType.STRING_TYPE;
        }

        return null;
    }

    @Override
    public Collection<String> supportedTypes() {
        return Arrays.asList("string");
    }

}
