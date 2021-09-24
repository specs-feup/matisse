/*
 * Copyright 2012 Specs.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. under the License.
 */

package org.specs.MatlabToCTester;

import java.io.File;

/**
 * Global data fields for MatlabToCTester.
 *
 * @author Pedro Pinto 
 */
public class MatlabToCTesterGlobalData {

    public final File baseInputFolder;
    public final File baseOutputFolder;

    /**
     * @param baseInputFolder
     * @param baseOutputFolder
     */
    public MatlabToCTesterGlobalData(File baseInputFolder, File baseOutputFolder) {
       this.baseInputFolder = baseInputFolder;
       this.baseOutputFolder = baseOutputFolder;
    }

}
