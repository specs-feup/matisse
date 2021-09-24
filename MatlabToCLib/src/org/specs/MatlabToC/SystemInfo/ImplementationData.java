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

package org.specs.MatlabToC.SystemInfo;

import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.Processor.MProcessor;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.StatementProcessor;
import org.specs.MatlabToC.MatlabRules.EndToNumel;
import org.specs.MatlabToC.MatlabRules.InfoCollector;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 * 
 */
public class ImplementationData {

    // TODO: Replace typesMap with setup
    private final TypesMap typesMap;
    private final ProjectMFiles projectMFiles;
    private final MatlabFunctionTable builtInPrototypes;
    private String baseCFilename;
    private final MProcessor mprocessor;
    private final StatementProcessor statementProcessor;

    // private final Setup setup;

    // private final MProcessorWithInputs mprocessorWithInputs;

    /**
     * @param typesMap
     * @param projectMFiles
     * @param builtInPrototypes
     * 
     *            TODO: Check where TypesMap is coming from in the calling functions, to see if it can be replaced by
     *            setup
     */
    public ImplementationData(TypesMap typesMap, ProjectMFiles projectMFiles, LanguageMode languageMode,
            MatlabFunctionTable builtInPrototypes,
            DataStore setup) {

        this.typesMap = typesMap;
        this.projectMFiles = projectMFiles;
        this.builtInPrototypes = builtInPrototypes;

        baseCFilename = null;
        mprocessor = newMProcessor();
        statementProcessor = new StatementProcessor(languageMode, setup);
        // mprocessorWithInputs = newMProcessorWithInputs();
        // this.setup = setup;
    }

    /**
     * @return the statementProcessor
     */
    public StatementProcessor getStatementProcessor() {
        return statementProcessor;
    }

    /**
     * @return the mprocessor
     */
    public MProcessor getMprocessor() {
        return mprocessor;
    }

    /**
     * @return the mprocessorWithInputs
     */
    /*
    public MProcessorWithInputs getMprocessorWithInputs() {
    return mprocessorWithInputs;
    }
    */

    /**
     * @return
     */
    private static MProcessor newMProcessor() {
        MProcessor mprocessor = new MProcessor();

        InfoCollector infoCollector = new InfoCollector();

        mprocessor.addStatementRule(infoCollector);
        // Resolve 'end' tokens.
        // Apply before transformations such as MultipleSetToFor, which change the meaning of the code
        mprocessor.addStatementRule(new EndToNumel(infoCollector.getInfo()));
        // mprocessor.addStatementRule(new UnfoldAssignWithTemp());

        // mprocessor.addStatementRule(new MultipleSetToFor());

        // mprocessor.addStatementRule(new ConcatIdiom());

        return mprocessor;
    }

    /**
     * @return
     */
    /*
    private MProcessorWithInputs newMProcessorWithInputs() {
    MProcessorWithInputs mprocessor = new MProcessorWithInputs();
    
    mprocessor.addStatementRule(new SumForVectors());
    
    return mprocessor;
    }
    */
    /**
     * @param baseCFilename
     *            the baseCFilename to set
     */
    public void setBaseCFilename(String baseCFilename) {
        this.baseCFilename = baseCFilename;
    }

    /**
     * @return the baseCFilename
     */
    public String getBaseCFilename() {
        return baseCFilename;
    }

    /**
     * @return the builtInPrototypes
     */
    public MatlabFunctionTable getBuiltInPrototypes() {
        return builtInPrototypes;
    }

    /**
     * @return the projectMFiles
     */
    public ProjectMFiles getProjectMFiles() {
        return projectMFiles;
    }

    /**
     * @return the typesMap
     */
    public TypesMap getTypesMap() {
        return typesMap;
    }

    public LanguageMode getLanguageMode() {
        return statementProcessor.getLanguageMode();
    }
}
