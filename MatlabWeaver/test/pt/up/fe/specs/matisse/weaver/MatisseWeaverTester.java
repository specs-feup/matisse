/**
 * Copyright 2016 SPeCS.
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

package pt.up.fe.specs.matisse.weaver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.lara.interpreter.joptions.config.interpreter.LaraiKeys;
import org.lara.interpreter.joptions.config.interpreter.VerboseLevel;
import org.lara.interpreter.joptions.keys.FileList;
import org.lara.interpreter.joptions.keys.OptionalFile;
import org.lara.interpreter.weaver.interf.WeaverEngine;
import org.suikasoft.jOptions.Interfaces.DataStore;

import larai.LaraI;
import pt.up.fe.specs.matisse.weaver.options.MWeaverKeys;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class MatisseWeaverTester {

    private static final boolean DEBUG = false;

    private static final String WORK_FOLDER = "matisse_weaver_output";

    private final String basePackage;

    private String srcPackage;
    private String resultPackage;

    public MatisseWeaverTester(String basePackage) {
        this.basePackage = basePackage;

        srcPackage = null;
        resultPackage = null;
    }

    /**
     * 
     * @param checkWeavedCodeSyntax
     * @return the previous value
     */
    // public MatisseWeaverTester setCheckWeavedCodeSyntax(boolean checkWeavedCodeSyntax) {
    // this.checkWeavedCodeSyntax = checkWeavedCodeSyntax;
    //
    // return this;
    // }

    public MatisseWeaverTester setResultPackage(String resultPackage) {
        this.resultPackage = sanitizePackage(resultPackage);

        return this;
    }

    public MatisseWeaverTester setSrcPackage(String srcPackage) {
        this.srcPackage = sanitizePackage(srcPackage);

        return this;
    }

    private String sanitizePackage(String packageName) {
        String sanitizedPackage = packageName;
        if (!sanitizedPackage.endsWith("/")) {
            sanitizedPackage += "/";
        }

        return sanitizedPackage;
    }

    private ResourceProvider buildCodeResource(String codeResourceName) {
        StringBuilder builder = new StringBuilder();

        builder.append(basePackage);
        if (srcPackage != null) {
            builder.append(srcPackage);
        }

        builder.append(codeResourceName);

        return () -> builder.toString();
    }

    public void test(String laraResource, String... codeResource) {
        test(laraResource, Arrays.asList(codeResource));
    }

    public void test(String laraResource, List<String> codeResources) {
        SpecsLogs.msgInfo("\n---- Testing '" + laraResource + "' ----\n");
        List<ResourceProvider> codes = SpecsCollections.map(codeResources, this::buildCodeResource);

        File log = runMatlabWeaver(() -> basePackage + laraResource, codes);
        String logContents = SpecsIo.read(log);

        StringBuilder expectedResourceBuilder = new StringBuilder();
        expectedResourceBuilder.append(basePackage);
        if (resultPackage != null) {
            expectedResourceBuilder.append(resultPackage);
        }
        expectedResourceBuilder.append(laraResource).append(".txt");

        String expectedResource = expectedResourceBuilder.toString();
        // String expectedResource = basePackage + laraResource + ".txt";
        if (!SpecsIo.hasResource(expectedResource)) {
            SpecsLogs.msgInfo("Could not find resource '" + expectedResource
                    + "', skipping verification. Actual output:\n" + logContents);
            return;
        }

        assertEquals(normalize(SpecsIo.getResource(expectedResource)), normalize(logContents));
    }

    /**
     * Normalizes endlines
     *
     * @param resource
     * @return
     */
    private static String normalize(String string) {
        return SpecsStrings.normalizeFileContents(string, true);
        // return string.replaceAll("\r\n", "\n");
    }

    private File runMatlabWeaver(ResourceProvider lara, List<ResourceProvider> code) {
        // Prepare folder
        File workFolder = SpecsIo.mkdir(WORK_FOLDER);
        SpecsIo.deleteFolderContents(workFolder);

        // Prepare files
        code.forEach(resource -> resource.write(workFolder));
        File laraFile = lara.write(workFolder);

        DataStore data = DataStore.newInstance("MatisseWeaverTest");

        // Set LaraI configurations
        data.add(LaraiKeys.LARA_FILE, laraFile);
        data.add(LaraiKeys.OUTPUT_FOLDER, workFolder);
        data.add(LaraiKeys.WORKSPACE_FOLDER, FileList.newInstance(workFolder));
        data.add(LaraiKeys.VERBOSE, VerboseLevel.warnings);
        data.add(LaraiKeys.LOG_JS_OUTPUT, Boolean.TRUE);
        data.add(LaraiKeys.LOG_FILE, OptionalFile.newInstance(getWeaverLog().getAbsolutePath()));

        // Set MatisseWeaver configurations
        data.add(MWeaverKeys.CHECK_SYNTAX, Boolean.TRUE);

        // Set CxxWeaver configurations
        // data.set(ClavaOptions.STANDARD, standard);
        // data.set(ClavaOptions.FLAGS, compilerFlags);
        // data.set(CxxWeaverOption.CHECK_SYNTAX, checkWeavedCodeSyntax);
        // data.set(CxxWeaverOption.DISABLE_CLAVA_INFO, true);

        MWeaver weaver = new MWeaver(DataStore.newInstance("MWeaver Tester"));
        try {
            boolean result = LaraI.exec(data, weaver);
            // Check weaver executed correctly
            assertTrue(result);
        } catch (Exception e) {
            // After LaraI execution, static weaver is unset, and it is no longer safe to use the weaver instance
            // unless we set the weaver again
            if (weaver.getApp() != null) {
                weaver.setWeaver();
                SpecsLogs.msgInfo("Current code:\n" + weaver.getApp().getCode());
                WeaverEngine.removeWeaver();
            } else {
                SpecsLogs.msgInfo("App not created");
            }

            throw new RuntimeException("Problems during weaving", e);
        }

        // Return true if result is 0
        // return result == 0 ? true : false;

        return getWeaverLog();
    }

    public static File getWorkFolder() {
        return new File(WORK_FOLDER);
    }

    public static File getWeaverLog() {
        return new File(WORK_FOLDER, "test.log");
    }

    public static void clean() {
        if (DEBUG) {
            return;
        }

        // Delete CWeaver folder
        File workFolder = MatisseWeaverTester.getWorkFolder();
        if (workFolder.isDirectory()) {
            SpecsIo.deleteFolderContents(workFolder, true);
            SpecsIo.delete(workFolder);
        }

        // Delete weaver files
        // ClangAstParser.getTempFiles().stream()
        // .forEach(filename -> new File(filename).delete());
    }

}
