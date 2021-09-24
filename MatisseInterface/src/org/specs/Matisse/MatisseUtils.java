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

package org.specs.Matisse;

import java.io.File;

import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.app.AppPersistence;
import org.suikasoft.jOptions.persistence.XmlPersistence;

/**
 * @author Joao Bispo
 * 
 */
public class MatisseUtils {

    private static final AppPersistence PERSISTENCE = new XmlPersistence();

    public static AppPersistence getPersistence() {
	return MatisseUtils.PERSISTENCE;
    }

    /**
     * @param weaverSetupFile
     * @return
     */
    public static DataStore loadAspect(File aspectDataFile) {
	return getPersistence().loadData(aspectDataFile);
    }

    /*
    public static SetupOptions loadSetupHelper(File setupFile) {
    return new SetupOptions(loadAspect(setupFile));
    }
    */

    /**
     * @param aspectDataFile
     * @param aspectData
     */
    public static void saveAspect(File aspectDataFile, DataStore aspectData) {
	getPersistence().saveData(aspectDataFile, aspectData, true);

    }
}
