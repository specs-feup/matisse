/*
 * Copyright 2013 SPeCS.
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.lara.interpreter.utils.LaraIUtils;
import org.lara.interpreter.weaver.interf.AGear;
import org.lara.interpreter.weaver.interf.JoinPoint;
import org.lara.interpreter.weaver.options.WeaverOption;
import org.lara.language.specification.LanguageSpecification;
import org.lara.language.specification.dsl.LanguageSpecificationV2;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.collect.Lists;

import larai.LaraI;
import pt.up.fe.specs.lara.langspec.LangSpecsXmlParser;
import pt.up.fe.specs.matisse.weaver.VariableTypes.TypesParser;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AApp;
import pt.up.fe.specs.matisse.weaver.abstracts.weaver.AMWeaver;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MApp;
import pt.up.fe.specs.matisse.weaver.options.MWeaverKeys;
import pt.up.fe.specs.matisse.weaver.options.MWeaverOption;
import pt.up.fe.specs.matisse.weaver.resources.LaraResource;
import pt.up.fe.specs.matisse.weaver.resources.MWeaverResource;
import pt.up.fe.specs.matisse.weaver.resources.MatisseApiResource;
import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.parsing.arguments.ArgumentsParser;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * Abstract Weaver Implementation for MWeaver. The implementation of the abstract methods is mandatory!
 *
 * @author Lara C.
 */
public class MWeaver extends AMWeaver {
    private static final String TEMP_WEAVING_FOLDER = "__matisse_weaved";

    private static final String MATISSE_ICON_PATH = "matisse/matisse_icon_300dpi.png";

    private static final String FILENAME_ASPECT_DATA = "mweaver.aspectdata";
    private static final String FILENAME_TYPES = "mweaver_typeDef.txt";

    private static final Set<String> LANGUAGES = SpecsCollections.asSet("matlab", "octave");

    public static String getAspectDataFilename() {
        return MWeaver.FILENAME_ASPECT_DATA;
    }

    public static String getTypesFilename() {
        return MWeaver.FILENAME_TYPES;
    }

    public static LanguageSpecificationV2 buildLanguageSpecification() {
        return LangSpecsXmlParser.parse(MWeaverResource.JOINPOINTS, MWeaverResource.ARTIFACTS, MWeaverResource.ACTIONS,
                true);
    }

    // Fields
    private MApp root;
    private List<File> mFiles;
    // private File baseFolder;
    private Optional<File> outputDir = Optional.empty();
    private final DataStore settings;

    // Constructors

    public MWeaver() {
        this(MatlabToCOptionUtils.newDefaultSettings());
    }

    public MWeaver(DataStore settings) {
        this.settings = settings;

        this.root = null;
        this.mFiles = null;
        // this.baseFolder = null;
    }

    public List<File> getMFiles() {
        return mFiles;
    }

    public MApp getApp() {
        return root;
    }

    // Methods

    /**
     * Set a file/folder in the weaver if it is valid file/folder type for the weaver.
     *
     * @param source
     *            the file with the source code
     * @return true if the file type is valid
     */
    @Override
    public boolean begin(List<File> source, File outputDir, DataStore args) {
        MWeaverUtils.setWeaver(this);

        this.outputDir = Optional.of(outputDir);

        if (source == null) {
            return false;
        }

        settings.addAll(args);

        // System.out.println("Is TOM enabled? " + options.isTom());

        List<File> mFiles = Collections.emptyList();

        if (!args.get(MWeaverKeys.DISABLE_CODE_PARSING)) {
            mFiles = getSourceFiles(source);

            // If no files found, return
            if (mFiles.isEmpty()) {
                SpecsLogs.msgInfo("[MWeaver] Could not find any .m files, terminating.");
                return false;
            }
        }

        buildAndSetNewRoot(mFiles);
        this.mFiles = mFiles;
        // this.baseFolder = getBaseFolder(this.mFiles);
        return true;

    }

    private List<File> getSourceFiles(List<File> source) {
        List<File> mFiles = Lists.newArrayList();
        for (File file : source) {

            boolean isLink = Files.isSymbolicLink(file.toPath());
            if (isLink) {
                continue;
            }

            if (file.isDirectory()) {

                mFiles.addAll(SpecsIo.getFilesRecursive(file, "m"));
            } else {

                if (SpecsIo.getExtension(file).equals("m")) {
                    mFiles.add(file);
                }
            }
        }
        return mFiles;
    }

    /*
    private static File getBaseFolder(List<File> sources) {
        Preconditions.checkArgument(!sources.isEmpty(), "Needs at least one source specified (file or folder)");
    
        File firstSource = sources.stream()
                .filter(source -> source.exists())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Needs to specify at least one source (file or folder) that exists, found none. Input sources:"
                                + sources));
    
        if (firstSource.isDirectory()) {
            return firstSource;
        }
    
        if (firstSource.isFile()) {
            return firstSource.getParentFile();
        }
    
        throw new RuntimeException("Could not process source '" + firstSource + "'");
    }
    */

    /**
     * Instantiate {@link AApp} according to the approach ( {@link MatlabNode} or {@link TomToken} )
     *
     * @param source
     */
    private void buildAndSetNewRoot(List<File> sources) {
        root = new MApp(sources, settings); // Join point root using MatlanToken
    }

    private MApp buildNewRoot(List<File> sources) {
        return new MApp(sources, settings); // Join point root using MatlanToken
    }

    public DataStore getSettings() {
        return settings;
    }

    /**
     * Returns the program root to be used by the weaver for the selects
     *
     * @return interface implementation for the join point root/program
     */
    @Override
    public JoinPoint select() {
        return root;
    }

    /**
     * Closes the weaver and specifies the output directory location if the weaver generates new file(s)
     *
     * @param outputDir
     *            output directory for the generated file(s)
     * @return if close was successful
     */
    @Override
    public boolean close() {

        if (settings.get(MWeaverKeys.CHECK_SYNTAX)) {
            SpecsLogs.msgInfo("Checking weaved code syntax...");
            rebuildAst(false);
        }

        // VarType typeDef = null;
        // TomToken tomRoot = null;
        // MatlabNode matlabToken = null;

        // TODO: Adapt to App, now MFile does not contain the aspectData
        boolean writeStatus = true;

        if (settings.get(MWeaverKeys.AUTOMATIC_CODE_GENERATION)) {
            root.write(outputDir.get());
        }

        /*
        for (AFile aFile : root.selectFile()) {
            StringBuilder outputFilePath = new StringBuilder(outputDir.get().getAbsolutePath());
            outputFilePath.append(System.getProperty("file.separator"));
        
            // AFile aFile = root.selectFile().get(0);
            String fileName = aFile.getName();
        
            MFile mFile = (MFile) aFile;
            matlabToken = mFile.getMatlabRoot();
            typeDef = mFile.getTypeDef();
        
            // Process options
            // aspectData = process(mFile);
        
            outputFilePath.append(fileName);
            File outputFile = new File(outputFilePath.toString());
            File typeFile = new File(outputFilePath.toString().replace(".m", "_typeDef.txt"));
        
            List<? extends AFunction> functionsJp = aFile.selectFunction();
            String mainFunction = null;
            if (!functionsJp.isEmpty()) {
                mainFunction = functionsJp.get(0).getName();
            }
        
            if (typeDef != null && !typeDef.isEmpty()) {
        
                String typeDefString = typeDef.toString(mainFunction);
                SpecsIo.write(typeFile, typeDefString);
        
           
        
            }
        
            if (matlabToken == null) {
                throw new RuntimeException("MatlabNode is null!");
            }
        
            String matlabCode = matlabToken.getCode();
            boolean lastWriteStatus = SpecsIo.write(outputFile, matlabCode);
        
            // Update until there is a false result
            if (writeStatus) {
                writeStatus = lastWriteStatus;
            }
        }
        */

        File aspectDataFile = new File(outputDir.get().getAbsolutePath(), getAspectDataFilename());

        // MatisseUtils.saveAspect(aspectDataFile, root.getAspectData());

        // TODO: Saving the aspect file is not working!
        // MatisseUtils.saveAspect(aspectDataFile, settings);

        // System.out.println("ASPECTS DATA:" + globalAspectData);

        // Write type def
        File typeFile = new File(outputDir.get().getAbsolutePath(), getTypesFilename());

        // TypesMap typesMap = root.getAspectData().get(MatisseKeys.TYPE_DEFINITION);
        TypesMap typesMap = settings.get(MatisseKeys.TYPE_DEFINITION);

        Map<List<String>, Set<List<String>>> scopes = new HashMap<>();
        for (List<String> key : typesMap.getKeys()) {
            for (int i = 0; i < key.size() - 1; ++i) {
                List<String> subKeyParent = key.subList(0, i);
                List<String> subKeyChild = key.subList(0, i + 1);

                Set<List<String>> scopeSiblings = scopes.get(subKeyParent);
                if (scopeSiblings == null) {
                    scopeSiblings = new HashSet<>();
                    scopes.put(subKeyParent, scopeSiblings);
                }
                scopeSiblings.add(subKeyChild);
            }
        }
        Map<List<String>, Map<String, String>> types = new HashMap<>();
        for (List<String> key : typesMap.getKeys()) {
            List<String> scope = key.subList(0, key.size() - 1);
            String varName = SpecsCollections.last(key);

            Map<String, String> vars = types.get(scope);
            if (vars == null) {
                vars = new HashMap<>();
                types.put(scope, vars);
            }

            VariableType type = typesMap.getSymbol(key);
            vars.put(varName, TypesParser.generate(type));
        }

        StringBuilder typeStringBuilder = new StringBuilder();
        generateScopes(typeStringBuilder, scopes, types, Collections.emptyList());

        String typeDefString = typeStringBuilder.toString().trim() + "\n";
        SpecsIo.write(typeFile, typeDefString);

        // Delete temporary weaving folder, if exists
        SpecsIo.deleteFolder(new File(TEMP_WEAVING_FOLDER));
        /*
        
        aspectDataF
        types.
        String typeDefString = typeDef.toString(mainFunction);
        IoUtils.write(typeFile, typeDefString);
         */
        return writeStatus;
    }

    private void generateScopes(StringBuilder typeStringBuilder,
            Map<List<String>, Set<List<String>>> scopes,
            Map<List<String>, Map<String, String>> types,
            List<String> currentScope) {

        String indentation = String.join("", Collections.nCopies(currentScope.size(), "\t"));

        Map<String, String> typesInScope = types.get(currentScope);
        if (typesInScope != null) {
            for (String key : typesInScope.keySet()) {
                String value = typesInScope.get(key);

                typeStringBuilder.append('\n');
                typeStringBuilder.append(indentation);
                typeStringBuilder.append(key);
                typeStringBuilder.append(": ");
                typeStringBuilder.append(value);
            }
        }

        Set<List<String>> subScopes = scopes.get(currentScope);
        if (subScopes != null) {
            for (List<String> subScope : subScopes) {
                typeStringBuilder.append("\n");
                typeStringBuilder.append(indentation);
                typeStringBuilder.append("scope ");
                typeStringBuilder.append(SpecsCollections.last(subScope));
                typeStringBuilder.append(" {");

                generateScopes(typeStringBuilder, scopes, types, subScope);

                typeStringBuilder.append("\n");
                typeStringBuilder.append(indentation);
                typeStringBuilder.append("}");
            }
        }
    }

    private void rebuildAst(boolean update) {
        // Write current tree to a temporary folder
        File tempFolder = SpecsIo.mkdir(TEMP_WEAVING_FOLDER);
        SpecsIo.deleteFolderContents(tempFolder, true);
        // System.out.println("TEMP FOLDER: " + tempFolder.getAbsolutePath());
        root.write(tempFolder);
        // List<File> srcFolders = SpecsCollections.concat(tempFolder, SpecsIo.getFoldersRecursive(tempFolder));
        // MApp rebuiltApp = buildNewRoot(srcFolders);
        List<File> srcFiles = getSourceFiles(Arrays.asList(tempFolder));
        MApp rebuiltApp = buildNewRoot(srcFiles);

        // Base folder is now the temporary folder
        if (update) {
            this.root = rebuiltApp;
            // baseFolder = tempFolder;
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // SuikaProperty.applyProperties();
        SpecsSystem.programStandardInit();
        // SuikaProperty.ShowStackTrace.applyProperty("true");

        boolean success = run(args, MatlabToCOptionUtils.newDefaultSettings());

        // System.out.println("MWEAVER:"+success);

        if (!success) {
            throw new RuntimeException("Problems while running MatlabWeaver");
            // LoggingUtils.msgInfo(ProcessUtils.getErrorString());
        }
        /*
        if (success) {
            return 0;
        } else {
        
            //System.out.println("MWEAVER RETURNIN:"+(-1));
            return -1;
        }
         */
    }

    public static boolean run(String[] args, DataStore globalProperties) {

        List<String> argsW = new ArrayList<>(Arrays.asList(args));

        return run(argsW, globalProperties);
    }

    public static boolean run(List<String> argsW, DataStore globalProperties) {

        // Map<String, String> resources = LaraIUtils.getResourcesWithName(MWeaver.class,
        // "../../../MatlabSpecification");
        Map<String, String> resources = SpecsFactory.newHashMap();
        for (MWeaverResource resource : MWeaverResource.values()) {
            resources.put(resource.getResource(), SpecsIo.getResource(resource));
        }

        /*
        File specDir = new File("resources");
        for (MWeaverResource resource : MWeaverResource.values()) {
            IoUtils.resourceCopy(resource.getResource(), specDir, false);
        }
         */

        // Copy LARA resources
        // LARA files are copied to the temporary working folder, that way they can be found
        copyResources(SpecsIo.getWorkingDir());

        /*
        if (!specDir.exists())
            specDir.mkdirs();
        for (String str : resources.keySet()) {
            File outFile = new File(specDir, str);
            IoUtils.write(outFile, resources.get(str));
        }
         */

        // Add includes folder to arguments
        // addIncludesFolder(argsW);
        /*
        	if (argsW.size() != 0) {
        	    argsW.add("-w");
        	    argsW.add("org.specs.mweaver.MWeaver");
        	}
        
         */
        String args[] = {};

        // boolean success = LaraI.exec(argsW.toArray(args));
        // LanguageSpecification ls = LanguageSpecification.newInstance(MWeaverResource.class, true);

        boolean success = LaraI.exec(argsW.toArray(args), new MWeaver(globalProperties));

        return success;
    }

    /**
     * @param includeFolders
     * @param destinationFolder
     */

    public static void copyResources(File outputFolder) {
        // Copy LaraI.properties to current folder
        // File jarFolder = new File(LaraIUtils.getJarFoldername());
        // IoUtils.resourceCopy(MiscResource.LARAI_PROPERTIES.getResource(), jarFolder, false);

        // Create library folder, copy libraries there
        // File includesFolder = getIncludesFolder();
        // File aspectsFolder = new File(includesFolder, "aspects");
        // IoUtils.resourceCopy(LaraResource.class, getAspectsFolder(), false);
        // File laraFolder
        // File laraFolder = new File(LaraI.getJarFoldername(), "includes/aspects");
        SpecsIo.resourceCopy(LaraResource.class, outputFolder, false);
        // IoUtils.resourceCopy(LaraResource.class, getAspectsFolder(), false);
        // IoUtils.resourceCopy(JavascriptResource.class, includesFolder, true);
    }

    public static File getIncludesFolder() {
        return new File(LaraIUtils.getJarFoldername(), "includes");
    }

    public static File getAspectsFolder() {
        return new File(getIncludesFolder(), "aspects");
    }

    @Override
    public List<AGear> getGears() {
        return Collections.emptyList();
    }

    @Override
    public LanguageSpecification getLanguageSpecification() {
        return LanguageSpecification.newInstance(MWeaverResource.JOINPOINTS,
                MWeaverResource.ARTIFACTS, MWeaverResource.ACTIONS, true);
    }

    @Override
    public List<WeaverOption> getOptions() {
        return MWeaverOption.getOptions();
    }

    @Override
    public String getName() {
        return "Matisse - MATLAB Weaver v1.0";
    }

    @Override
    public List<ResourceProvider> getAspectsAPI() {
        List<ResourceProvider> resources = new ArrayList<>();

        // resources.addAll(ResourceProvider.getResources(LaraApiResource.class));
        // resources.addAll(ResourceProvider.getResources(LaraCoreApiResource.class));
        resources.addAll(MatisseLaraApis.getApis());
        resources.addAll(ResourceProvider.getResources(MatisseApiResource.class));

        return resources;
    }

    @Override
    public List<Class<?>> getImportableClasses() {
        List<Class<?>> importableClasses = new ArrayList<>();
        importableClasses.addAll(MatisseLaraApis.getImportableClasses());
        importableClasses.addAll(Arrays.asList(ArgumentsParser.class, MWeaverLauncher.class));

        return importableClasses;
        // return MatisseLaraApis.getImportableClasses();
    }

    @Override
    public ResourceProvider getIcon() {
        return getWeaverIcon();
    }

    public static ResourceProvider getWeaverIcon() {
        return () -> MATISSE_ICON_PATH;
    }

    public static MWeaver getMWeaver() {
        return (MWeaver) getThreadLocalWeaver();
    }

    @Override
    public Set<String> getLanguages() {
        return LANGUAGES;
    }
}