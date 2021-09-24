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

package org.specs.matisselib.passmanager.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNodePass.BuilderPassData;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.passmanager.PreTypeContext;
import org.specs.matisselib.services.NamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.servicesv2.ScopeService;
import org.specs.matisselib.servicesv2.ScopedNamingService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Extends PassData to include keys mapped to a specific {@code FunctionIdentification}.
 * 
 * @author JoaoBispo
 *
 */
public abstract class APassManagerData extends BuilderPassData implements PassManagerData {

    // Using BuilderPassData internally, to have access to setValue
    private final Map<FunctionIdentification, BuilderPassData> dataMap;
    // Key is to be given to services that need to access PassContext
    private final DataKey<PreTypeContext> contextKey;
    // private final PassDataBuilderProvider passDataProvider;
    private Optional<FunctionIdentification> currentId;

    public APassManagerData(DataStore setupBuilder, DataKey<PreTypeContext> contextKey) {
        this(setupBuilder, new HashMap<>(), contextKey, Optional.empty());
        /*
        	super(setupBuilder);
        
        	dataMap = new HashMap<>();
        	this.contextKey = contextKey;
        	// this.passDataProvider = passDataProvider;
        	currentId = Optional.empty();
        	*/
    }

    protected APassManagerData(DataStore setupBuilder, Map<FunctionIdentification, BuilderPassData> dataMap,
            DataKey<PreTypeContext> contextKey, Optional<FunctionIdentification> currentId) {

        super(setupBuilder);

        this.dataMap = dataMap;
        this.contextKey = contextKey;

        this.currentId = currentId;
    }

    protected Map<FunctionIdentification, BuilderPassData> getDataMap() {
        return dataMap;
    }

    protected DataKey<PreTypeContext> getContextKey() {
        return contextKey;
    }

    @Override
    public Optional<FunctionIdentification> setFunctionId(Optional<FunctionIdentification> functionId) {
        Optional<FunctionIdentification> previousId = currentId;

        currentId = functionId;

        return previousId;
    }

    @Override
    public Optional<PassManagerData> newData(Optional<FunctionIdentification> functionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasFunctionId() {
        return currentId.isPresent();
    }

    /**
     * Creates a new APassData, to be used when referring to the given function id.
     * 
     * @param functionId
     * @return
     */
    // /protected abstract APassData newPassData(FunctionIdentification functionId);

    /**
     * 
     * @return the name of the parent pass data
     */
    protected String getParentName() {
        return super.getName();
    }

    // protected <T> T getParentValue(DataKey<T> key) {
    // return super.getValue(key);
    // }

    /******
     * PassData Override
     ******/

    /**
     * If id is set, adds to the pass of the current id. Otherwise, adds to this pass data.
     */
    @Override
    public <T, E extends T> DataStore add(DataKey<T> key, E value) {
        if (currentId.isPresent()) {
            getCurrentIdData().add(key, value);
            return this;
            // PassData data = dataMap.get(currentId.get());
            // data.add(key, value);
            // return;
        }

        super.add(key, value);

        return this;
    }

    /**
     * If id is set, replaces in the pass of the current id. Otherwise, replaces in this pass data.
     */
    @Override
    public <T, E extends T> void replace(DataKey<T> key, E value) {
        if (currentId.isPresent()) {
            getCurrentIdData().replace(key, value);
            // PassData data = dataMap.get(currentId.get());
            // data.replace(key, value);
            return;
        }

        super.replace(key, value);
    }

    // No need to override, the default implementation directly calls getValue
    // public <T> T get(DataKey<T> key);
    /**
     * 
     * If id is set, returns value in the pass of the current id. Otherwise, return in this pass data.
     */
    /*
    @Override
    public <T> T get(DataKey<T> key) {
    if (currentId.isPresent()) {
        return getCurrentIdData().get(key);
        // PassData data = dataMap.get(currentId.get());
        // return data.get(key);
    }
    
    return super.get(key);
    }
    */

    /******
     * CleanSetup Override
     ******/

    @Override
    public String getName() {
        if (currentId.isPresent()) {
            return getCurrentIdData().getName();
        }

        return super.getName();
    }

    /**
     * If a {@code FunctionIdentification} is set, first searches for the value in the function PassData. If there is
     * not key there, it searches in the general PassData.
     */
    @Override
    public <T> T get(DataKey<T> key) {
        if (currentId.isPresent()) {
            // Check if key exists in id pass data
            DataStore idData = getCurrentIdData();

            if (idData.hasValue(key)) {
                return idData.get(key);
            }

        }

        if (super.hasValue(key)) {
            return super.get(key);
        }

        if (currentId.isPresent()) {
            throw new RuntimeException("Could not find key '" + key + "' neither in '" + getCurrentIdData().getName()
                    + "' nor in '" + super.getName() + "'");
        }

        throw new RuntimeException("Could not find key '" + key + "' in '" + super.getName() + "'");
    }

    /******
     * CleanSetupBuilder Override
     ******/

    @Override
    public APassManagerData set(DataStore setup) {
        if (currentId.isPresent()) {
            getCurrentIdData().set(setup);
        }

        super.set(setup);

        return this;
    }

    /**
     * If an id is set, sets the value of that pass data. Otherwise, sets the value in the general pass data.
     */
    @Override
    // public <T, E extends T> Optional<T> set(DataKey<T> key, E value) {
    public <T, E extends T> APassManagerData set(DataKey<T> key, E value) {
        if (currentId.isPresent()) {
            // return getCurrentIdData().set(key, value);
            getCurrentIdData().set(key, value);
            return this;
        }

        // return super.set(key, value);
        super.set(key, value);
        return this;
    }

    /**
     * If an id is set, sets the values of that pass data. Otherwise, sets the value in the general pass data.
     */
    /*
    @Override
    public void setValues(DataView setup) {
    if (currentId.isPresent()) {
        getCurrentIdData().setValues(setup);
    }
    
    super.setValues(setup);
    }
    */

    /*******
     * Private Functions
     ******/

    /**
     * 
     * @param functionId
     * @return
     */
    // The stream must remain open, so that others can write to it.
    private BuilderPassData getData(FunctionIdentification functionId) {

        BuilderPassData data = dataMap.get(functionId);
        if (data != null) {
            return data;
        }

        // We have to be careful inside this function, to avoid infinite recursions
        // Disable function id
        Optional<FunctionIdentification> previousId = setFunctionId(Optional.empty());

        // Create new data for the function
        // data = new CommonPassData(buildSetupName(functionId));
        // data = passDataProvider.newInstance(functionId, this);
        // data = passDataProvider.newInstance(functionId);

        data = new CommonPassData(buildSetupName(getParentName(), functionId));

        // Data for reporter
        // PrintStream stream = get(MatisseInit.PRINT_STREAM);
        FileNode fileNode = get(MatisseInit.MFILES_SERVICE).getFileNodeSafe(functionId.getFile());

        // Add reporter
        NodeReportService report = new NodeReportService(get(MatisseInit.PRINT_STREAM), functionId.getFile(),
                fileNode.getOriginalCode());
        data.add(PassManager.NODE_REPORTING, report);

        // Add scope service
        ScopeService scopeService = new ScopeService(functionId.getFile(), this, contextKey);
        data.add(PassManager.SCOPE_SERVICE, scopeService);

        // Get unit for naming service
        MatlabUnitNode unit = new PassManager(contextKey, "APassManagerData_getData") {
        }.getUnit(functionId, this);

        // Add naming service
        NamingService namingService = new ScopedNamingService(scopeService, unit);
        data.add(PreTypeInferenceServices.COMMON_NAMING, namingService);

        dataMap.put(functionId, data);

        // Enable function id again
        setFunctionId(previousId);

        return data;
    }

    /**
     * If an id is set, returns the corresponding PassData. Otherwise, throws an exception.
     * 
     * @return the PassData of the current id
     */
    private BuilderPassData getCurrentIdData() {
        return getData(currentId.get());
    }

    private static String buildSetupName(String setupName, FunctionIdentification functionId) {
        StringJoiner joiner = new StringJoiner("_");

        joiner.add(setupName);
        joiner.add(functionId.getFileNoExtension());

        // Add name of the function if different from the name of the file
        if (!functionId.getFileNoExtension().equals(functionId.getName())) {
            joiner.add(functionId.getName());
        }

        return joiner.toString();
    }

}
