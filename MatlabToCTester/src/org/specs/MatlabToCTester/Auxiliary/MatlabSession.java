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

package org.specs.MatlabToCTester.Auxiliary;

import java.io.File;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabSession {

    private static MatlabSession GLOBAL_SESSION = null;

    /**
     * @return the GLOBAL_SESSION
     */
    public static MatlabSession getGlobalSession() {
	// return MatlabSession.newInstance();

	if (MatlabSession.GLOBAL_SESSION == null) {
	    MatlabSession.GLOBAL_SESSION = MatlabSession.newInstance();
	}

	if (MatlabSession.GLOBAL_SESSION != null && !MatlabSession.GLOBAL_SESSION.proxy.isConnected()) {
	    MatlabSession.GLOBAL_SESSION.exit();

	    MatlabSession.GLOBAL_SESSION = MatlabSession.newInstance();
	}

	return MatlabSession.GLOBAL_SESSION;

    }

    private final MatlabProxy proxy;

    private MatlabSession(MatlabProxy proxy) {
	this.proxy = proxy;
    }

    /**
     * 
     */
    public static MatlabSession newInstance() {

	// create proxy
	MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
		.setUsePreviouslyControlledSession(false).setHidden(true).build();
	MatlabProxyFactory factory = new MatlabProxyFactory(options);
	MatlabProxy proxy;

	try {
	    proxy = factory.getProxy();
	} catch (MatlabConnectionException e) {
	    SpecsLogs.msgInfo("Could not start MATLAB:" + e.getMessage());
	    return null;
	}

	return new MatlabSession(proxy);
    }

    public boolean runScript(File scriptFolder, String scriptName) {
	// call user-defined function (must be on the path)
	try {

	    this.proxy.eval("fprintf('Running Script " + scriptName + " in folder "
		    + scriptFolder.getPath().replace('\\', '/') + "\\n');");
	    this.proxy.eval("clear;");
	    // proxy.eval("fprintf('Running Script " + scriptName + " in folder "
	    // + scriptFolder.getName() + "\\n');");

	    this.proxy.eval("current_folder = pwd;");
	    this.proxy.eval("cd '" + scriptFolder.getAbsolutePath() + "'");
	    // proxy.eval("addpath('" + scriptFolder.getAbsolutePath() + "')");
	    this.proxy.feval(scriptName);
	    this.proxy.eval("cd(current_folder);");
	    this.proxy.eval("fprintf('Done\\n\\n');");
	    return true;
	    // proxy.eval("rmpath('\" + scriptFolder.getAbsolutePath() + \"')");
	} catch (MatlabInvocationException e) {
	    SpecsLogs.msgLib("Could not run script '" + scriptName + "' on path '"
		    + scriptFolder.getAbsolutePath() + "':" + e.getMessage());
	    return false;
	}
    }

    public void exit() {
	try {
	    this.proxy.exit();
	} catch (MatlabInvocationException e) {
	    SpecsLogs.msgInfo("Exception while exiting:" + e.getMessage());
	}
    }

}
