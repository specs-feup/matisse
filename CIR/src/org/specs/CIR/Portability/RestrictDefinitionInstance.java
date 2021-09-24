package org.specs.CIR.Portability;

import org.specs.CIR.FunctionInstance.Instances.StructInstance;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class RestrictDefinitionInstance extends StructInstance {

    public static final String HEADER = PortabilityHeader.LOCATION + ".h";

    public RestrictDefinitionInstance() {
	super("restrict", PortabilityHeader.LOCATION, getResourceCode());
    }

    private static final ThreadSafeLazy<String> DEFINITION_CODE = new ThreadSafeLazy<>(
	    () -> SpecsIo.getResource(PortabilityResource.RESTRICT));

    private static String getResourceCode() {
	return DEFINITION_CODE.getValue();
    }

}
