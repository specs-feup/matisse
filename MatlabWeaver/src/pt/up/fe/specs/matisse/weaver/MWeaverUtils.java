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

package pt.up.fe.specs.matisse.weaver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.Matisse.MatisseUtils;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

/**
 * @author Joao Bispo
 * 
 */
public class MWeaverUtils {

    private static final String MWEAVER_WORKSPACE = "mweaver_workspace";
    private static final String MWEAVER_OUTPUT = "mweaver_output";

    private static ThreadLocal<MWeaver> WEAVER = new ThreadLocal<>();

    public static void setWeaver(MWeaver mWeaver) {
        WEAVER.set(mWeaver);
    }

    public static MWeaver getWeaver() {
        return WEAVER.get();
    }

    /**
     * 
     * @param outputDir
     * @param logFile
     * @param laraFile
     * @return true if no exception occurred, false otherwise
     */
    public static boolean run(File workspace, File outputDir, File logFile, File laraFile,
            DataStore globalProperties) {

        String outputFoldername = outputDir.getAbsolutePath();

        List<String> args = new ArrayList<>();

        args.add(laraFile.getAbsolutePath());
        // args.add("--verbose");
        // args.add("2");
        args.add("-p");
        args.add(workspace.getAbsolutePath());
        args.add("-i");
        // args.add(workspace.getAbsolutePath()+";"+workspace.getAbsolutePath()+"\\scripts");
        args.add(workspace.getAbsolutePath());
        args.add("-o");
        args.add(outputFoldername);
        // args.add("-l");
        // args.add(logFile.getAbsolutePath());
        args.add("-j");
        // args.add("-aw");
        // args.add("tom");

        boolean success = false;
        try {
            success = MWeaver.run(args.toArray(new String[args.size()]), globalProperties);
        } catch (Exception e) {
            throw e;
        }

        return success;
    }

    public static MWeaverRun runMWeaver(LanguageMode languageMode, List<File> matlabFiles, File aspectFile,
            List<File> includeFolders,
            File outputFolder, DataStore globalProperties, boolean verbose) {

        // If includes folder is null, replace with empty list
        if (includeFolders == null) {
            includeFolders = Collections.emptyList();
        }

        // Create and clean MWeaver workspace
        // File mweaverWorkspace = IoUtils.safeFolder(outputFolder, MWEAVER_WORKSPACE);
        File mweaverWorkspace = getWorspace(outputFolder);
        if (mweaverWorkspace == null) {
            throw new RuntimeException("Could not create MATLAB Weaver workspace folder");
        }
        SpecsIo.deleteFolderContents(mweaverWorkspace);

        // Create and clean MWeaver output
        File mweaverOutput = SpecsIo.mkdir(outputFolder, MWeaverUtils.MWEAVER_OUTPUT);
        if (mweaverOutput == null) {
            throw new RuntimeException("Could not create MATLAB Weaver output folder");
        }
        SpecsIo.deleteFolderContents(mweaverOutput);

        // Copy MATLAB files to MWeaver workspace
        List<File> weavedFiles = SpecsFactory.newArrayList();
        for (File matlabFile : matlabFiles) {
            File weavedFile = new File(mweaverWorkspace, matlabFile.getName());
            SpecsIo.copy(matlabFile, weavedFile);
            weavedFiles.add(weavedFile);
        }

        // Generate arguments for MWeaver
        List<String> args = getArguments(languageMode, mweaverWorkspace, mweaverOutput, aspectFile, includeFolders,
                verbose);

        try {
            MWeaver.run(args.toArray(new String[args.size()]), globalProperties);
        } catch (Exception e) {
            SpecsLogs.warn("Exception in Matlab Weaver:" + e.getMessage(), e);
            return null;
        }

        // Get Weaver setup
        File weaverDataFile = SpecsIo.existingFile(mweaverOutput, MWeaver.getAspectDataFilename());

        // Merge options
        DataStore matlabWeaverConfig = DataStore.newInstance("MWeaverOptions");
        matlabWeaverConfig.addAll(MatisseUtils.loadAspect(weaverDataFile));

        return new MWeaverRun(mweaverOutput, weavedFiles, matlabWeaverConfig);
    }

    /**
     * @param mweaverWorkspace
     * @param mweaverOutput
     * @param aspectFile
     * @param includeFolders
     * @param verbose
     * @return
     */
    private static List<String> getArguments(LanguageMode languageMode, File mweaverWorkspace, File mweaverOutput,
            File aspectFile,
            List<File> includeFolders, boolean verbose) {

        List<String> args = new ArrayList<>();

        args.add(aspectFile.getAbsolutePath());

        args.add("-L");
        args.add(languageMode.name());

        if (verbose) {
            args.add("-d");
        } else {
            args.add("--verbose");
            args.add("2");
        }
        /*
        args.add("--verbose");
        if (verbose) {
            args.add("3");
        } else {
            args.add("2");
        }
        */

        // args.add("--verbose");
        // args.add("3");

        // Debug Mode
        // args.add("-d");

        args.add("-p");
        args.add(mweaverWorkspace.getAbsolutePath());

        String includes = buildIncludeArg(includeFolders);
        if (!includeFolders.isEmpty()) {
            args.add("-i");
            args.add(includes);
        }

        args.add("-o");
        args.add(mweaverOutput.getAbsolutePath());
        // args.add("-l");
        // args.add(logFile.getAbsolutePath());
        // args.add("-j");
        return args;
    }

    private static String buildIncludeArg(List<File> includeFolders) {
        // Prepare include folders
        StringBuilder builder = new StringBuilder();

        if (!includeFolders.isEmpty()) {
            builder.append(includeFolders.get(0).getAbsolutePath());
        }

        for (int i = 1; i < includeFolders.size(); i++) {
            builder.append(SpecsIo.getUniversalPathSeparator()).append(includeFolders.get(i).getAbsolutePath());
        }

        return builder.toString();
    }

    /**
     * @param outputFolder
     * @return
     */
    public static File getWorspace(File outputFolder) {
        return SpecsIo.mkdir(outputFolder, MWeaverUtils.MWEAVER_WORKSPACE);
    }

}
