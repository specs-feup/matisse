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

package org.specs.MatlabProcessor.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.utilities.LineStream;

/**
 * A list of files to ignore.
 * 
 * @author JoaoBispo
 *
 */
public class IgnoreList {

    private final static String CRAWL_IGNORE_EXTENSION = "crawlerignore";

    private final List<Pattern> patterns;
    private final File rootFolder;

    private int ignoreCounter;

    private IgnoreList(List<Pattern> patterns, File rootFolder) {
	this.patterns = patterns;
	this.rootFolder = rootFolder;
	ignoreCounter = 0;
    }

    /**
     * Builds an empty list.
     */
    private IgnoreList(File rootFolder) {
	this(new ArrayList<>(), rootFolder);
    }

    public IgnoreList add(IgnoreList list) {
	this.patterns.addAll(list.patterns);
	return this;
    }

    /**
     * Parses a 'crawlerignore' file.
     * 
     * <p>
     * A 'crawlerignore' file contains one regular expression per line, and support one-line comments that start with
     * '//'.
     * 
     * @param file
     * @param rootFolder
     * @return
     */
    private static IgnoreList newInstance(File file, File rootFolder) {

	List<Pattern> patterns = LineStream.newInstance(file).stream()
		// Trim lines
		.map(line -> line.trim())
		// Ignore comments
		.filter(line -> !line.startsWith("//"))
		// Add rootFolder, if necessary
		.map(line -> parseLine(line, file, rootFolder))
		.map(line -> Pattern.compile(line + "$", Pattern.MULTILINE))
		.collect(Collectors.toList());

	return new IgnoreList(patterns, rootFolder);
    }

    private static String parseLine(String line, File file, File rootFolder) {
	// Check if file is in the root folder
	File parentFile = file.getParentFile();
	if (parentFile.equals(rootFolder)) {
	    return line;
	}

	StringBuilder builder = new StringBuilder();

	// Add path to root folder
	builder.append(SpecsIo.getRelativePath(parentFile, rootFolder));
	if (!line.startsWith("/")) {
	    builder.append('/');
	}
	builder.append(line);

	return builder.toString();
    }

    public static IgnoreList newInstanceFromFolder(File folder) {
	return newInstanceFromFolder(folder, true);
    }

    /**
     * 
     * @param folder
     * @param recursive
     *            if true, searches for ignore files recursively. Otherwise, searches for ignore files only on the root
     *            folder
     * @return
     */
    public static IgnoreList newInstanceFromFolder(File folder, boolean recursive) {

	// Get files with the correct extension
	List<File> files = null;
	if (recursive) {
	    files = SpecsIo.getFilesRecursive(folder, CRAWL_IGNORE_EXTENSION);
	} else {
	    files = SpecsIo.getFiles(folder, CRAWL_IGNORE_EXTENSION);
	}

	// IgnoreList newlist = IoUtils.getFilesRecursive(folder, CRAWL_IGNORE_EXTENSION).stream()
	IgnoreList newlist = files.stream()
		// Transform ignore file into an IgnoreList
		.map(file -> newInstance(file, folder))
		// Merge ignore lists
		.reduce(new IgnoreList(folder), (acc, newList) -> acc.add(newList));

	return newlist;
    }

    public List<File> filter(List<File> files) {
	List<File> filteredFiles = new ArrayList<>();

	for (File file : files) {
	    // Get relative path
	    String path = SpecsIo.getRelativePath(file, rootFolder);
	    if (ignore(path)) {
		ignoreCounter++;
		continue;
	    }

	    filteredFiles.add(file);
	}

	return filteredFiles;
    }

    private boolean ignore(String path) {
	// System.out.println("CHECKING " + path);
	return patterns.stream()
		.filter(pattern -> SpecsStrings.matches(path, pattern))
		.findFirst()
		.isPresent();
    }

    /**
     * 
     * @return the number of ignored files
     */
    public int getIgnored() {
	return ignoreCounter;
    }
}
