package org.specs.MatlabToC.Functions.Strings;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum MatlabStringFunctionResources implements ResourceProvider {
    STRING_FROM_CSTRING,
    STRING_FROM_CHAR_MATRIX,
    EQUALS;

    private static final String BASE_PATH = "templates/matlabstring/";

    @Override
    public String getResource() {
        return BASE_PATH + name().toLowerCase(Locale.UK) + ".c";
    }
}
