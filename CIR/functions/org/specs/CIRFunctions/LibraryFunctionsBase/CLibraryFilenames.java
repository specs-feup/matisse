package org.specs.CIRFunctions.LibraryFunctionsBase;

public enum CLibraryFilenames {

    General("lib/general");
    
    private CLibraryFilenames(String name){
	this.cFilename = name;
    }
    
    public String getFilename(){
	return cFilename;
    }

    private final String cFilename;
}
