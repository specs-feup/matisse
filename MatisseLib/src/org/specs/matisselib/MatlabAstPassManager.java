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

package org.specs.matisselib;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.passmanager.data.PassManagerData;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.providers.ResourceProvider;

public interface MatlabAstPassManager extends KeyUser {

    boolean getAppliedPreTypeInferencePasses();

    FunctionIdentification getRootFunction();

    DataStore getPassDataForPreTypeInference(FunctionIdentification functionId);

    Optional<MatlabNode> getFunctionNode(FunctionIdentification functionId);

    boolean hasFunctionNode(FunctionIdentification functionId);

    Optional<List<FunctionIdentification>> getFunctionsIn(String file);

    void applyPreTypeInferencePasses(String rootFile);

    FunctionIdentification processResource(ResourceProvider resource);

    FunctionIdentification processResource(String name, String code);

    void log(String message);

    String getPassLog();

    default PassManager getPassManager() {
	throw new NotImplementedException("Not implemented for class '" + getClass() + "'");
    }

    default PassManagerData getPassData() {
	throw new NotImplementedException("Not implemented for class '" + getClass() + "'");
    }

    void setReportStream(PrintStream reportStream);
}