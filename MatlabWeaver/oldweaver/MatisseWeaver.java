package org.specs.oldweaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import larai.LaraI;

import org.reflect.larai.IWeaver;
import org.reflect.larai.Joinpoint;
import org.specs.MatlabIR.MatlabToken;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabProcessor.MatlabGenerator.MatlabGeneratorUtils;
import org.specs.oldweaver.utils.Action;
import org.specs.oldweaver.utils.Select;
import org.specs.tom.api.TomTokenManager;
import org.specs.tom.strategies.InsertCode;
import org.specs.tom.tomtokenutilstom.list.types.TomToken;
import org.suikasoft.SharedLibrary.IoUtils;
import org.suikasoft.SharedLibrary.ProcessUtils;

/**
 * Copyright 2012 SPeCS Research Group.
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

/**
 * @author Tiago
 * 
 */
public class MatisseWeaver implements IWeaver {

    private File sourceFile;
    private MatlabToken root;
    private Joinpoint fileJoinpoint;

    public MatisseWeaver() {
	root = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.reflect.larai.IWeaver#getActions()
     */
    @Override
    public List<String> getActions() {
	List<String> actions = new ArrayList<String>();
	for (Action.Actions action : Action.Actions.values())
	    actions.add(action.toString());
	return actions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.reflect.larai.IWeaver#select(java.lang.Object, java.lang.String)
     */
    @Override
    public List<Joinpoint> select(Object parentJoinpoint, String joinpointClass) {
	try {
	    switch (Select.JoinPoints.valueOf(joinpointClass)) {
	    case file:
		return selectFile();
	    case function:
		return Select.function((MatlabToken) parentJoinpoint);
	    default:
		System.err.println("join point type not implemented");
		System.exit(-1);
	    }
	} catch (IllegalArgumentException e) {
	    System.err.println("join point type does not exist");
	}
	return null;
    }

    /**
     * Select the file join point
     * 
     * @return a list with one join point
     */
    private List<Joinpoint> selectFile() {
	List<Joinpoint> jpl = new ArrayList<Joinpoint>();
	jpl.add(fileJoinpoint);
	return jpl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.reflect.larai.IWeaver#action(java.lang.Object, java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void action(Object joinpointReference, String method, Object... args) {
	try {
	    switch (Action.Actions.valueOf(method)) {
	    case insert:
		if (!(args[1] instanceof Map<?, ?>)) {
		    throw new RuntimeException("Argument at index 1 has to be a map. It is a '"
			    + args[1].getClass() + "'");
		}
		Map<?, ?> parameters = (Map<?, ?>) args[1];
		String when = (String) args[0];
		Action.insert((MatlabToken) joinpointReference, when, parameters);
		break;
	    case def: // TODO - insert new typedef in the file/hashtable
		break;
	    default:
		System.err.println("Action not implemented");
		System.exit(-1);
	    }
	} catch (IllegalArgumentException e) {
	    System.err.println("Action does not exist");
	}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	ProcessUtils.programStandardInit();
	LaraI.exec(args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.reflect.larai.IWeaver#setFile(java.io.File)
     */
    @Override
    public boolean setFile(File sourceFile) {
	if (!sourceFile.getName().endsWith(".m"))
	    return false;
	createFileJP(sourceFile);
	return true;
    }

    private void createFileJP(File sourceFile) {
	this.sourceFile = sourceFile;
	this.root = MatlabProcessorUtils.fromMFile(this.sourceFile);
	this.fileJoinpoint = Select.file(this.root, sourceFile);
    }

//    private void sandbox(MatlabToken root) {
//	List<MatlabToken> statements = MatlabTokenUtils.getTokens(root, MTokenType.Statement);
//	for (MatlabToken t : statements) {
//
//	    // System.out.println("STATEMENT CONTENT: "+t.getContent().getClass()+"\n\n"+t.toString());
//	}
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.reflect.larai.IWeaver#close(java.io.File)
     */
    @Override
    public boolean close(File outputDir) {
	//System.out.println("BEFORE: "+MatlabGeneratorUtils.generate(root));
	// Call Tom to replace comments with the corresponding actions
	TomToken tomToken = TomTokenManager.generateTomToken(root);
	InsertCode iCode = new InsertCode(tomToken);
	// Sets the option display comment in the strategy
	iCode.setOptions(InsertCode.OPTION_DISPLAY_COMMENT, "" + false);
	tomToken = iCode.apply();
	MatlabToken newRoot = TomTokenManager.generateMatlabToken(tomToken);
	this.root = newRoot;

	// Generate Matlab code and save in the file outputDir/<fileName>
	StringBuilder outputFilePath = new StringBuilder(outputDir.getAbsolutePath());
	outputFilePath.append(System.getProperty("file.separator"));
	outputFilePath.append(sourceFile.getName());
	File outputFile = new File(outputFilePath.toString());
	String matlabCode = MatlabGeneratorUtils.generate(root);
	//System.out.println("AFTER: "+MatlabGeneratorUtils.generate(root));
	boolean writeStatus = IoUtils.write(outputFile, matlabCode);
	return writeStatus;
    }

}
