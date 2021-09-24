/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabProcessor.MatlabParser;

import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Optional;

import org.specs.MatlabIR.Exceptions.CodeParsingException;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.Processor.TreeTransformException;
import org.specs.MatlabIR.Processor.TreeTraversalStrategy;
import org.specs.MatlabProcessor.Exceptions.ParserErrorException;
import org.specs.MatlabProcessor.MatlabParser.Rules.PostParsingRules;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.utilities.LineStream;

/**
 * Helper class which converts MATLAB code into a MATLAB-IR tree, a file at a time.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabParser {

    // private final ParserMode parserMode;
    private int totalLines;
    private int nonEmptyLines;

    private final Optional<PrintStream> messageStream;
    private final LanguageMode languageMode;

    public MatlabParser() {
        this(LanguageMode.MATLAB);
    }

    /**
     * Creates a Parser that uses System.err as the default message stream.
     */
    public MatlabParser(LanguageMode languageMode) {
        // this(System.err);
        this(languageMode, Optional.of(System.err));
    }

    public MatlabParser(PrintStream messageStream) {
        this(LanguageMode.MATLAB, Optional.ofNullable(messageStream));
    }

    public MatlabParser(LanguageMode languageMode, PrintStream messageStream) {
        this(languageMode, Optional.ofNullable(messageStream));
    }

    public MatlabParser(Optional<PrintStream> messageStream) {
        this(LanguageMode.MATLAB, messageStream);
    }

    /**
     * 
     */
    public MatlabParser(LanguageMode languageMode, Optional<PrintStream> messageStream) {
        // As default, use MATLAB mode
        // parserMode = ParserMode.MATLAB;
        totalLines = 0;
        nonEmptyLines = 0;

        this.languageMode = languageMode;
        this.messageStream = messageStream;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getNonEmptyLines() {
        return nonEmptyLines;
    }

    /**
     * Convenience method that accepts a File.
     * 
     * @param mFile
     * @return
     */
    public FileNode parse(File mFile) {

        try (LineStream reader = LineStream.newInstance(mFile)) {
            return parse(reader);
        }
    }

    public FileNode parse(ResourceProvider resource) {
        try (LineStream reader = LineStream.newInstance(resource, false)) {
            // System.out.println("FILENAME:" + reader.getFilename());
            return parse(reader);
        }
    }

    public Optional<FileNode> parseTry(String mCode) {
        try {
            return Optional.of(parse(mCode));
        } catch (RuntimeException e) {
            SpecsLogs.warn("Problems during parsing", e);
            return Optional.empty();
        }
    }

    /**
     * Convenience method which accepts a string, and uses FileNode.getNoFilename() as default name.
     * 
     * @param mCode
     * @return
     */
    public FileNode parse(String mCode) {
        return parse(LineStream.newInstance(new StringReader(mCode), Optional.empty()));
    }

    public FileNode parse(SimpleFile file) {
        return parse(LineStream.newInstance(new StringReader(file.getContents()),
                Optional.of(file.getFilename())));
    }

    private FileNode parse(LineStream lineReader) {
        // String line = null;

        String filename = lineReader.getFilename().orElse(FileNode.getNoFilename());

        MatlabLineParser lineParser = new MatlabLineParser(filename, languageMode, messageStream);

        // StringBuilder originalCode = new StringBuilder();

        // Transform M-file into a list of statements
        for (String line : lineReader.getIterable()) {

            // Generate intermediate representation for the given line
            try {
                lineParser.addLine(line);
            } catch (CodeParsingException e) {
                // Rethrow it
                throw new CodeParsingException(line, lineParser.getLines(), e);
            } catch (Exception e) {
                throw new ParserErrorException("Could not parse line " + lineParser.getLines() + "\n" + line, e);
            }

        }
        FileNode fileNode = lineParser.getFileNode();

        // Apply post-parsing rules
        try {
            TreeTraversalStrategy.bottomUp.applyRuleList(fileNode, PostParsingRules.getRules());
        } catch (TreeTransformException e) {
            throw new CodeParsingException("Erros during post-parsing rules", e);
            // LoggingUtils.msgInfo(e.getMessage());
            // return null;
        }

        // Save MatlabLineParser line count
        totalLines = lineParser.getLines();
        nonEmptyLines = lineParser.getNonEmptyLines();

        return fileNode;
    }
}
