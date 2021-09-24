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

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.GlobalOptionsUtils;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;

/**
 * Global setup definition for program MatlabToCTester.
 *
 * @author Pedro Pinto
 */
public enum MatlabToCTesterGlobalSetup implements SetupFieldEnum {

    BaseInputFolder(FieldType.string), BaseOutputFolder(FieldType.string);

    /**
     * Reads from global Preferences associated to this class.
     * 
     * @return
     */
    public static MatlabToCTesterGlobalData getData() {
       SetupFieldEnum anEnum = BaseInputFolder;
       SetupData setupData = GlobalOptionsUtils.loadData(anEnum.getClass());

       SetupAccess setup = new SetupAccess(setupData);

       File baseInputFolder = new File(setup.getString(BaseInputFolder));
       if(!baseInputFolder.isDirectory()) {
	       baseInputFolder = null;
       }
	
       File baseOutputFolder = new File(setup.getString(BaseOutputFolder));
       if(!baseOutputFolder.isDirectory()) {
	       baseOutputFolder = null;
       }

       return new MatlabToCTesterGlobalData(baseInputFolder, baseOutputFolder);
    }

    private MatlabToCTesterGlobalSetup(FieldType fieldType) {
       this.fieldType = fieldType;
    }

    @Override
    public FieldType getType() {
       return fieldType;
    }

    @Override
    public String getSetupName() {
       return "MatlabToCTesterGlobalOptions";
    }

    /**
     * INSTANCE VARIABLES
     */
    private final FieldType fieldType;

}
