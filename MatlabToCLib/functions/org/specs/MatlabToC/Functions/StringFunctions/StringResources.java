package org.specs.MatlabToC.Functions.StringFunctions;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum StringResources implements ResourceProvider {
    STRSPLIT;

    private static final String BASE_PATH = "templates/string/";

    @Override
    public String getResource() {
        return BASE_PATH + name().toLowerCase(Locale.UK) + ".c";
    }

}
