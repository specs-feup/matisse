package org.specs.matisselib.helpers;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;

public class TypesMapUtils {
    public static List<String> getVariableTypeScope(FunctionIdentification function) {
        List<String> scope = new ArrayList<>();
        scope.add(function.getFileNoExtension());
        if (!function.isFileMainFunction()) {
            scope.add(function.getName());
        }
        return scope;
    }
}
