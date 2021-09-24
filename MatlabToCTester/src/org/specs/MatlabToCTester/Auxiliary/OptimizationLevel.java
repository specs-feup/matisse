package org.specs.MatlabToCTester.Auxiliary;

public enum OptimizationLevel {

    O0,
    O1,
    O2,
    O3,
    Os,
    Og;

    public String getFlag() {
	return "-" + this.name();
    }
}
