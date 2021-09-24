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

package org.specs.matisselib.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.providers.StringProvider;

public class UserFileProviderService {

    private final LanguageMode languageMode;
    private final Map<String, StringProvider> availableFiles;
    private final Map<String, FileNode> parsedFiles;

    public UserFileProviderService(LanguageMode languageMode, Map<String, StringProvider> availableFiles) {
        this.languageMode = languageMode;
        this.availableFiles = availableFiles;
        this.parsedFiles = new HashMap<>();
    }

    /**
     * Returns a parsed M-file from the given m-file name. If no m-file with the given name is available, returns an
     * empty Optional.
     * 
     * @param name
     *            the name of an M-file, including .m extension
     * @return a MATLAB-IR tree with the parsed file
     */
    public Optional<FileNode> getFileNode(String name) {
        assert name != null;

        if (parsedFiles.containsKey(name)) {
            return Optional.of(parsedFiles.get(name));
        }

        if (availableFiles.containsKey(name)) {
            FileNode node = new MatlabParser(languageMode).parse(SimpleFile.newInstance(name, availableFiles.get(name)
                    .getString()));
            parsedFiles.put(name, node);
            return Optional.of(node);
        }

        return Optional.empty();
    }

    /**
     * Helper method which throws an exception if file is not found.
     * 
     * @param name
     * @return
     */
    public FileNode getFileNodeSafe(String name) {
        return getFileNode(name)
                .orElseThrow(() -> new IllegalArgumentException("No such file: '" + name + "'"));
    }

    public void addResourceFile(String name, StringProvider codeProvider) {
        availableFiles.put(name, codeProvider);
    }
}
