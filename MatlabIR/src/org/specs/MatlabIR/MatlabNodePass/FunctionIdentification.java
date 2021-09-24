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

package org.specs.MatlabIR.MatlabNodePass;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsIo;

/**
 * Identifies a function in a MATLAB file.
 * 
 * <p>
 * Functions are identified by name and by file. Currently only supports main functions and sub-functions (e.g., not
 * supporting nested functions).
 * 
 * @author JoaoBispo
 *
 */
public final class FunctionIdentification {
    private final String file;
    private final String name;

    public FunctionIdentification(String file, String name) {
        Preconditions.checkArgument(file != null);
        Preconditions.checkArgument(name != null);
        Preconditions.checkArgument(!name.isEmpty(), "Name should not be empty");
        Preconditions.checkArgument(FileNode.isValidFilename(file),
                "Filename should end with '.m', but received " + file);
        Preconditions.checkArgument(!file.endsWith(".m.m"), "Filename should not end with doubled '.m'. Received: "
                + file);

        this.file = file;
        this.name = name;

    }

    public FunctionIdentification(String file) {
        this(file, SpecsIo.removeExtension(file));
    }

    /**
     * 
     * @return the name of the file, including the .m extension
     */
    public String getFile() {
        return file;
    }

    /**
     * 
     * @return the name of the file, without the .m extension
     */
    public String getFileNoExtension() {
        return SpecsIo.removeExtension(getFile());
    }

    /**
     * 
     * @return the name of the function
     */
    public String getName() {
        return name;
    }

    public boolean isFileMainFunction() {
        return getFileNoExtension().equals(getName());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof FunctionIdentification)) {
            return false;
        }

        FunctionIdentification otherId = (FunctionIdentification) other;

        return file.equals(otherId.file) &&
                name.equals(otherId.name);
    }

    @Override
    public int hashCode() {
        return file.hashCode() ^ name.hashCode();
    }

    @Override
    public String toString() {
        return getFile().toString() + ":" + name;
    }
}
