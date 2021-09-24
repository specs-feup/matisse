/**
 * Copyright 2014 SPeCS.
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

package org.specs.matlabprocessorprinter;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;
import pt.up.fe.specs.z3helper.UnsupportedPlatformException;
import pt.up.fe.specs.z3helper.Z3LibraryLoader;

public class Program {
    public static void main(String[] args) {
	SpecsSystem.programStandardInit();
	SpecsProperty.ShowStackTrace.applyProperty("true");

	new Thread(() -> {
	    try {
		Z3LibraryLoader.loadNativeLibraries();
	    } catch (UnsupportedPlatformException | IOException e) {
		SpecsLogs.warn("Error message:\n", e);
	    }
	}).run();

	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		try {
		    UIManager.setLookAndFeel(
			    UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    // Don't care.
		}

		new MatlabViewerFrame().setVisible(true);
	    }
	});
    }
}
