package org.specs.matlabtocl.v2.codegen;

import com.google.common.base.Preconditions;

public class RequiredCLExtensionAnnotation {
    private final String extension;

    public RequiredCLExtensionAnnotation(String extension) {
        Preconditions.checkArgument(extension != null);

        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return "[RequiredCLExtension " + extension + "]";
    }
}
