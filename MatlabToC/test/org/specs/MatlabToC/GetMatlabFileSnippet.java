package org.specs.MatlabToC;

import java.io.File;
import java.util.List;

import org.junit.Test;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.LineStream;

public class GetMatlabFileSnippet {

    @Test
    public void test() {
	String pathFile = "C:\\temp_dir\\path.txt";
	try (LineStream lineReader = LineStream.newInstance(new File(pathFile))) {
	    String line = null;

	    String mFile = "pinv.m";

	    while ((line = lineReader.nextLine()) != null) {
		File folder = new File(line.trim());

		List<File> files = SpecsIo.getFiles(folder);
		for (File file : files) {
		    if (file.getName().equals(mFile)) {
			System.out.println("Found '" + mFile + "':" + file.getAbsolutePath());
			return;
		    }
		}
	    }

	    System.out.println("Could not find file " + mFile);
	}

    }

}
