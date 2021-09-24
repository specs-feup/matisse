package org.specs.matisselib.services;

import org.specs.matisselib.typeinference.TypedInstance;

public interface Logger {
    public void log(Object msg);

    public default void logStart(TypedInstance instance) {
        log("Starting " + instance.getFunctionIdentification().getName());
    }

    public default void logSkip(TypedInstance instance) {
        log("Skipping " + instance.getFunctionIdentification().getName());
    }
}
