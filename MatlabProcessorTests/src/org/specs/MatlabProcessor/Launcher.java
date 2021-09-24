/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabProcessor;

import java.io.File;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class Launcher {

    public static void main(String[] args) {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");

	// System.out.println(NumberFormat.getIntegerInstance().format(1_000_000));
	// System.out.println(ParseUtils.parseTime(113_000_000_000l));

	oneLiner();
	// fileLauncher();
	// deleteNonM();
	/*
	 * for (String line : LineReader.createLineReader(text)) { if
	 * (line.startsWith("*")) { System.out.println("<h3>" +
	 * line.substring(1) + "</h3>\n<ul>"); continue; }
	 * 
	 * if (line.isEmpty()) { System.out.println("</ul>"); continue; }
	 * 
	 * System.out.println("<li>" + line + "</li>"); }
	 */
    }

    // Interesting examples
    // String matlabString = "A = [1 +...\n2]";

    public static void oneLiner() {

	// String matlabString = "a=3; ...\n if isempty(results);return;end";
	// String matlabString =
	// "try T.image.time = str2num(XML_get_attr_value( imgEl,'time' ));catch T.image.time = 0; end";
	String matlabString = "A =[+1 -2]";

	MatlabNode matlabNode = new MatlabParser().parse(matlabString);
	// MatlabNode matlabNode = MatlabProcessorUtils
	// .fromMFile(new File(
	// "C:\\Users\\JoaoBispo\\Dropbox\\Code-Repositories\\SignalProcessing\\wavekit\\packet2\\wpmult.m"));

	String matlabStringAgain = matlabNode.getCode();

	System.out.println(matlabNode);
	System.out.println(matlabStringAgain);

    }

    public static void fileLauncher() {

	// try {
	// System.in.read();
	// } catch (IOException e) {
	// LoggingUtils.msgWarn("Error message:\n", e);
	// }

	// String filepath = "C:/Users/JoaoBispo/Copy/M Files/Employee.m";
	// String filepath = "C:/Users/joaobispo/Copy/M Files/function_no_end.m";
	// String filepath = "C:/Users/JoaoBispo/Copy/M Files/function_end.m";
	// Uses variable 'properties' inside methods block, however it is classified as a properties statement
	// Should disable inside class when in function, but how to detect that? Build ends during tokenizer?
	String filepath = "C:/Users/JoaoBispo/Copy/M Files/Github - M files/pmtk3/toolbox/SupervisedModels/decisionTrees/dectree.m";

	// String filepath =
	// "C:/Users/JoaoBispo/Copy/M Files/SourceForge MATLAB - M
	// files/FinMetrics-0.48/FinMetrics/+FM/@AssetUniverse/AssetUniverse.m";

	long tic = System.nanoTime();
	MatlabNode node = new MatlabParser().parse(new File(filepath));
	String code = node.getCode();
	// System.out.println("CODE:\n" + code);
	String code2 = new MatlabParser().parse(code).getCode();
	// String code2 = MatlabProcessorUtils.toMFile(MatlabProcessorUtils
	// .fromMFile(code, null));

	long toc = System.nanoTime();
	// System.out.println("TREE:\n" + node);
	if (!code.equals(code2)) {
	    System.out.println("CODE1:\n" + code);
	    System.out.println("CODE2:\n" + code2);
	}

	node.getDescendantsAndSelfStream().anyMatch(c -> {
	    if (c.isTemporary()) {
		System.out.println("UNDEFINED SYMBOL:" + c);
	    }

	    return true;
	});

	System.out.println("Time:" + SpecsStrings.parseTime(toc - tic));
    }

    public static void deleteNonM() {
	// Should manually check folder before for 7z, bz2, rar, tgz, zip, gz or
	// tar, in case there are M-files in
	// archived files

	File folder = new File(
		"C:\\Users\\JoaoBispo\\Work\\Matlab\\Github - M files");

	// Iterate over all files in folder, delete every file that has not .m
	// extension
	List<File> files = SpecsIo.getFilesRecursive(folder);

	System.out.println("----- Deleting files -------");
	files.stream().filter(file -> !SpecsIo.getExtension(file).equals("m"))
		.forEach(file -> {
		    System.out.println("Deleting:" + file.getAbsolutePath());
		    boolean success = file.delete();
		    if (!success) {
			System.out.println("!PROBLEM");
		    }
		});

	System.out.println("\n\n----- Deleting folders -------");
	// Delete empty folders after
	List<File> folders = SpecsIo.getFoldersRecursive(folder);
	folders.stream()
		.filter(emptyFolder -> emptyFolder.list().length == 0)
		.forEach(
			emptyFolder -> {
			    System.out.println("Deleting:"
				    + emptyFolder.getAbsolutePath());
			    boolean success = emptyFolder.delete();
			    if (!success) {
				System.out.println("!PROBLEM");
			    }
			});

    }

    /*
     * public static void galleryCreator() { File rootFolder = new
     * File("C:\\Users\\JoaoBispo\\Desktop\\gallery");
     * 
     * // Read remote root folder String remoteRoot = IoUtils.read(new
     * File(rootFolder, "config.txt"));
     * 
     * // Create a gallery based on the folders in the root List<File> folders =
     * IoUtils.getFolders(rootFolder); StringBuilder builder = new
     * StringBuilder(); for (File folder : folders) { // Get name of sub-gallery
     * String title = IoUtils.read(new File(folder, "config.txt"));
     * builder.append("<a id=\"" + folder.getName() + "\"><h2>" + title +
     * "</h2></a>\n"); builder.append("<div class=\"gallery\">\n");
     * 
     * // Create set with files found in "big" folder Set<String> bigImages =
     * new HashSet<>(); File bigFolder = new File(folder, "big"); if
     * (bigFolder.isDirectory()) { List<File> bigFiles =
     * IoUtils.getFiles(bigFolder); bigFiles.forEach(file ->
     * bigImages.add(file.getName())); }
     * 
     * // Add each image File thumbFolder = new File(folder, "thumb");
     * List<File> images = IoUtils.getFiles(thumbFolder); for (File image :
     * images) { StringBuilder imgBuilder = new StringBuilder(); String src =
     * remoteRoot + folder.getName() + "/thumb/" + image.getName(); //
     * http://specs.fe.up.pt/joanafb/<gallery_name>/<img>
     * imgBuilder.append("<img src=\"" + src + "\">");
     * 
     * // Check if it has big image if (bigImages.contains(image.getName())) {
     * String img = imgBuilder.toString(); imgBuilder = new StringBuilder();
     * String bigHref = remoteRoot + folder.getName() + "/big/" +
     * image.getName(); imgBuilder.append("<a href=\"" + bigHref +
     * "\" target=\"_blank\" >"); imgBuilder.append(img);
     * imgBuilder.append("</a>"); }
     * 
     * // Add to page code builder.append(imgBuilder.toString() + "\n"); }
     * 
     * // Finish sub-gallery builder.append("</div>\n");
     * builder.append("<hr>\n");
     * 
     * } System.out.println("CODE\n:" + builder.toString()); }
     */
}
