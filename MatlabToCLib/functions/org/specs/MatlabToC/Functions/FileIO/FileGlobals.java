package org.specs.MatlabToC.Functions.FileIO;

import org.specs.CIR.FunctionInstance.Instances.GlobalVariableInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Literal.LiteralType;
import org.specs.CIRTypes.Types.Pointer.PointerType;

public class FileGlobals {
    public static GlobalVariableInstance getFileResourcesGlobal() {
        VariableType filePtrType = new PointerType(LiteralType.newInstance("FILE*"));
        GlobalVariableInstance fileResourcesGlobal = new GlobalVariableInstance("MATISSE_file_resources", filePtrType);
        fileResourcesGlobal.addCustomDeclarationIncludes(SystemInclude.Stdio.getIncludeName());
        return fileResourcesGlobal;
    }
}
