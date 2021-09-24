/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.StubInstance;
import org.specs.CIR.Passes.CPass;
import org.specs.CIR.Passes.CRecipe;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * A complete C project.
 * 
 * @author Joao Bispo
 * 
 */
public class CProject {

    private final Map<String, FunctionInstance> userFunctions;
    private final CRecipe cRecipe;
    private final DataStore setup;

    private Map<String, FunctionInstance> collectedInstances;
    private Collection<String> stubs;
    private String baseUniqueFilename;

    public CProject(CRecipe recipe, DataStore setup) {
        userFunctions = SpecsFactory.newLinkedHashMap();
        collectedInstances = null;
        baseUniqueFilename = null;
        cRecipe = recipe;
        this.setup = setup;
    }

    /**
     * @param baseUniqueFilename
     *            the baseUniqueFilename to set
     */
    public void setBaseUniqueFilename(String baseUniqueFilename) {
        this.baseUniqueFilename = baseUniqueFilename;
    }

    /**
     * @return the baseUniqueFilename
     */
    public String getBaseUniqueFilename() {
        return baseUniqueFilename;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (FunctionInstance impl : userFunctions.values()) {
            builder.append("Function '" + impl.getCName() + "':\n");
            builder.append(impl.toString());
        }

        return builder.toString();
    }

    /**
     * @return the cFiles
     */
    public Map<String, FunctionInstance> getUserFunctions() {
        return userFunctions;
    }

    /**
     * @param implementation
     */
    public void addFunction(FunctionInstance implementation) {
        // Get C-file name
        String cFunctionName = implementation.getCName();

        // Check if file already exists
        FunctionInstance previousFunction = userFunctions.put(cFunctionName, implementation);
        if (previousFunction != null) {
            SpecsLogs.warn("Duplicated function '" + cFunctionName + "' in the project.");
        }
    }

    /**
     * Returns all instances used by this project.
     * 
     * @return
     */
    public Map<String, FunctionInstance> getAllInstances() {
        if (collectedInstances != null) {
            return collectedInstances;
        }

        collectedInstances = collectImplementations(this);

        for (FunctionInstance instance : collectedInstances.values()) {
            for (CPass pass : cRecipe.getPasses()) {
                pass.apply(instance, ProviderData.newInstance(setup));
            }
        }

        return collectedInstances;
    }

    /**
     * Returns a Collection with the stubs used in this project.
     * 
     * <p>
     * A stub is a special, empty FunctionInstance built for functions which could not be implemented.
     * 
     * @return
     */
    public Collection<String> getStubs() {
        if (stubs != null) {
            return stubs;
        }

        stubs = collectStubs(getAllInstances().values());
        return stubs;
    }

    /**
     * Collects all functions needed to implement the given CProject.
     * 
     * @param cproject
     * @return
     */
    private static Map<String, FunctionInstance> collectImplementations(CProject cproject) {

        // Create set
        Set<FunctionInstance> functionImplementations = SpecsFactory.newLinkedHashSet();

        // Iterate over functions in the project
        for (FunctionInstance impl : cproject.getUserFunctions().values()) {
            boolean isNewValue = functionImplementations.add(impl);
            if (!isNewValue) {
                SpecsLogs.msgInfo("Found duplicated implementation for function '"
                        + impl.getCName() + "'");
            }
        }

        return collectImplementations(functionImplementations);
    }

    /**
     * Recursively travel the code tree looking for function calls, collecting function implementations.
     * 
     * @param mFilesImplementations
     * @return
     */
    private static Map<String, FunctionInstance> collectImplementations(
            Collection<FunctionInstance> mFilesImplementations) {

        // Map<String, FunctionInstance> implementations = FactoryUtils.newHashMap();
        Map<String, FunctionInstance> implementations = SpecsFactory.newLinkedHashMap();

        // Add all user implementation to the map
        for (FunctionInstance userfunction : mFilesImplementations) {
            implementations.put(userfunction.getCName(), userfunction);
        }

        // Add implementations needed by each function
        for (FunctionInstance userFunction : mFilesImplementations) {
            List<FunctionInstance> calledFunctions = FunctionInstanceUtils
                    .getInstancesRecursive(userFunction);
            addFunctions(calledFunctions, implementations);
        }

        return implementations;
    }

    /**
     * @param calledFunctions
     * @param implementations
     */
    private static void addFunctions(List<FunctionInstance> calledFunctions,
            Map<String, FunctionInstance> implementations) {

        // Go over each called function
        for (FunctionInstance functionCall : calledFunctions) {

            // Add to map if not in map yet
            if (!implementations.containsKey(functionCall.getCName())) {
                implementations.put(functionCall.getCName(), functionCall);
            }
        }

    }

    /**
     * Collects stubs from function instances.
     * 
     * @param instances
     * @return
     */
    private static Collection<String> collectStubs(Collection<FunctionInstance> instances) {
        Set<String> stubs = SpecsFactory.newHashSet();
        for (FunctionInstance instance : instances) {
            if (!(instance instanceof StubInstance)) {
                continue;
            }
            stubs.add(instance.getCName());
        }

        return stubs;
    }

}
