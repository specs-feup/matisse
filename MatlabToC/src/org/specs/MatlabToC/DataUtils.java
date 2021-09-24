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

package org.specs.MatlabToC;

import java.io.File;

import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.persistence.XmlPersistence;

/**
 * @author Joao Bispo
 *
 */
public class DataUtils {

    private static final String OUTPUT_FILENAME = "results.m2c";

    /**
     * @param outputFolder
     * @param generalSetup
     */
    public static void saveData(File outputFolder, DataStore setup) {

        XmlPersistence persistence = new XmlPersistence();

        File outputFile = getOutputFile(outputFolder);

        persistence.saveData(outputFile, setup);
    }

    /**
     * @param outputFolder
     * @param generalSetup
     */
    // public static Setup loadData(File outputFolder) {
    //
    // XmlPersistence persistence = new XmlPersistence();
    //
    // File outputFile = getOutputFile(outputFolder);
    //
    // return new SimpleSetup(persistence.loadData(outputFile));
    // }

    /**
     * @param outputFolder
     * @return
     */
    private static File getOutputFile(File outputFolder) {
        return new File(outputFolder, DataUtils.OUTPUT_FILENAME);
    }

}
