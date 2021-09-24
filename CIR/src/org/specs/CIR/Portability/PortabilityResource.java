package org.specs.CIR.Portability;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum PortabilityResource implements ResourceProvider {
    RESTRICT,
    DLLEXPORT;

    private static final String PATH = "cirlib/portability/";

    @Override
    public String getResource() {
        return PATH + name().toLowerCase(Locale.UK) + ".h";
    }
}
