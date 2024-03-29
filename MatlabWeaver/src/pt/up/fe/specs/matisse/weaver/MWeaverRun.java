/**
 * Copyright 2013 SPeCS Research Group.
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

package pt.up.fe.specs.matisse.weaver;

import java.io.File;
import java.util.List;

import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 * 
 */
public class MWeaverRun {

    private final File outputFolder;
    private final List<File> matlabFiles;
    private final DataStore settings;

    /**
     * @param matlabFiles
     * @param setup
     */
    public MWeaverRun(File outputFolder, List<File> matlabFiles, DataStore settings) {
        this.outputFolder = outputFolder;
        this.matlabFiles = matlabFiles;
        this.settings = settings;
    }

    /**
     * @return the matlabFiles
     */
    public List<File> getMatlabFiles() {
        return matlabFiles;
    }

    public DataStore getSettings() {
        return settings;
    }

    public File getOutputFolder() {
        return outputFolder;
    }
}
