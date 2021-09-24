package org.specs.matlabweaver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import larai.LaraI;

import org.reflect.larai.IWeaver;
import org.reflect.larai.Joinpoint;
import org.specs.MatlabIR.MatlabToken;
import org.specs.MatlabProcessor.MatlabProcessorUtils;
import org.specs.MatlabProcessor.MatlabGenerator.MatlabGeneratorUtils;
import org.specs.matlabweaver.utils.Action;
import org.specs.matlabweaver.utils.Select;
import org.specs.matlabweaver.utils.VarType;
import org.specs.matlabweaver.utils.Select.JoinPoints;
import org.specs.tom.TomTokenUtils;
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
public class BackupMatlabWeaver implements IWeaver {

	private File sourceFile;
	private TomToken root;
	private Joinpoint fileJoinpoint;
	private VarType typeDef;

	public BackupMatlabWeaver() {
		root = null;
		setTypeDef(new VarType());
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
			TomToken parentToken = (TomToken) parentJoinpoint;
			joinpointClass = joinpointClass.toUpperCase();
			switch (Select.JoinPoints.valueOf(joinpointClass)) {
			case FILE:
				return selectFile();
			case FUNCTION:
				return Select.function(parentToken, root);
			case INPUT:
				return Select.input(parentToken, root);
			case OUTPUT:
				return Select.output(parentToken, root);
			case VAR:
				return Select.var(parentToken, JoinPoints.VAR, root);
			case BODY:
				return Select.body(parentToken, root);
			case STATEMENT:
				return Select.statement(parentToken, root);
			case FIRST:
				return Select.first(parentToken, root);
			case LAST:
				return Select.last(parentToken, root);
			case LOOP:
				return Select.loop(parentToken, root);
			case IF:
				return Select.If(parentToken, root);
			case THEN:
				return Select.then(parentToken, root);
			case ELSEIF:
				return Select.ElseIf(parentToken, root);
			case ELSE:
				return Select.Else(parentToken, root);
			case COMMENT:
				return Select.comment(parentToken, root);
			case SECTION:
				return Select.section(parentToken, root);
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
		this.fileJoinpoint = Select.file(this.root, sourceFile);
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
					throw new RuntimeException(
							"Argument at index 1 has to be a map. It is a '"
									+ args[1].getClass() + "'");
				}
				Map<?, ?> parameters = (Map<?, ?>) args[1];
				String when = (String) args[0];
				this.root = Action.insert(root, (TomToken) joinpointReference,
						when, parameters);
				break;
			case def:
				if (args.length != 2) {
					throw new RuntimeException(
							"Def action requires 2 arguments, " + args.length
									+ " was given.");
				}
				String attribute = (String) args[0];
				Object value = args[1];
				root = Action.def(root, (TomToken) joinpointReference,
						attribute, value, getTypeDef());
				break;
			default:
				System.err.println("Action not implemented");
				System.exit(-1);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// System.err.println();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//ProcessUtils.programStandardInit();
		List<String> argsW = new ArrayList<String>(Arrays.asList(args));
		if(args.length != 0){
			argsW.add("-w");
			argsW.add("org.specs.matlabweaver.MatlabWeaver");
		}
		LaraI.exec(argsW.toArray(args));
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
		MatlabToken javaIR = MatlabProcessorUtils.fromMFile(this.sourceFile);
		this.root = TomTokenUtils.generateTomToken(javaIR);

		this.fileJoinpoint = Select.file(this.root, sourceFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.reflect.larai.IWeaver#close(java.io.File)
	 */
	@Override
	public boolean close(File outputDir) {
		/*
		 * //System.out.println("BEFORE: "+MatlabGeneratorUtils.generate(root));
		 * // Call Tom to replace comments with the corresponding actions
		 * TomToken tomToken = TomTokenManager.generateTomToken(root);
		 * 
		 * MatlabToken newRoot = TomTokenManager.generateMatlabToken(tomToken);
		 * this.root = newRoot;
		 */
		// Generate Matlab code and save in the file outputDir/<fileName>
		MatlabToken newJavaIR = TomTokenUtils.generateMatlabToken(root);
		StringBuilder outputFilePath = new StringBuilder(
				outputDir.getAbsolutePath());
		outputFilePath.append(System.getProperty("file.separator"));
		outputFilePath.append(sourceFile.getName());
		File outputFile = new File(outputFilePath.toString());
		String matlabCode = MatlabGeneratorUtils.generate(newJavaIR);
		// System.out.println("AFTER: "+MatlabGeneratorUtils.generate(root));
		boolean writeStatus = IoUtils.write(outputFile, matlabCode);

		if (!typeDef.isEmpty()) {
			File typeFile = new File(outputFilePath.toString().replace(".m",
					"_typeDef.txt"));
			IoUtils.write(typeFile, typeDef.toString());
		}

		return writeStatus;
	}

	/**
	 * @return the typeDef
	 */
	public VarType getTypeDef() {
		return typeDef;
	}

	/**
	 * @param typeDef
	 *            the typeDef to set
	 */
	public void setTypeDef(VarType typeDef) {
		this.typeDef = typeDef;
	}

}
