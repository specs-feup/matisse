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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import pt.up.fe.specs.util.providers.StringProvider;

public class FileNodeContent {

    // private final File file;
    private final String filename;
    private final StringProvider originalCode;

    /**
     * @param filename
     * @param originalCode
     */
    public FileNodeContent(String filename, StringProvider originalCode) {
        // this.file = null;
        this.filename = filename;
        this.originalCode = originalCode;
    }

    /*
    public FileNodeContent(File file, StringProvider originalCode) {
        // this.filename = filename;
        this.file = file;
        this.filename = file.getName();
        this.originalCode = originalCode;
    }
    
    public File getFile() {
        return file;
    }
    */
    public String getFilename() {
        return filename;
    }

    public StringProvider getOriginalCode() {
        return originalCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        return result;
    }

    /**
     * Equals does not check the content of the original code, only the filename.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileNodeContent other = (FileNodeContent) obj;
        if (filename == null) {
            if (other.filename != null)
                return false;
        } else if (!filename.equals(other.filename))
            return false;
        return true;
    }

    /**
     * Returning empty string, to show nothing in the tree and not trigger test errors due to differences in source
     * names.
     */
    @Override
    public String toString() {
        return "";

        // return "\"" + getFilename() + "\"";
        // return "Filename:" + getFilename() + " (code ommited)";
    }
}
