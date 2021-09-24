package org.specs.MatlabToC.Functions.FileIO;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum FileResources implements ResourceProvider {
    FOPEN1,
    FCLOSE,
    REGISTER_FILE_RESOURCE,
    INITIALIZE_FILE_RESOURCES,
    MATISSE_FGETL;

    public static final String BASE_PATH = "templates/io/";

    @Override
    public String getResource() {
        return BASE_PATH + name().toLowerCase(Locale.UK) + ".c";
    }
}
