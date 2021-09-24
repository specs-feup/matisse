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

package org.specs.matlabweaver.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reflect.larai.Joinpoint;
import org.specs.tom.strategies.TomAttributes;
import org.specs.tom.strategies.TomSelect;
import org.specs.tom.tomtokenutilstom.list.types.TomToken;

public class Select {

    /**
     * Enumeration of the possible join points in Matisse (TOM tree)
     * 
     * @author Tiago
     * 
     */
    public static enum JoinPoints {
	FILE,
	FUNCTION,
	INPUT,
	OUTPUT,
	VAR,
	BODY,
	STATEMENT,
	FIRST,
	LAST,
	LOOP,
	IF,
	THEN,
	ELSEIF,
	ELSE,
	COMMENT,
	SECTION,
    };

    /**
     * Create a joinpoint for the source file
     * 
     * @param root
     *            the reference token
     * @param sourceFile
     *            the source code file
     * @return a Joinpoint relative to the source file
     */
    public static Joinpoint file(TomToken root, File sourceFile) {
	Joinpoint jpFile = new Joinpoint(root);
	Map<String, Object> fileAttrs = new HashMap<String, Object>();
	fileAttrs.put("name", sourceFile.getName());
	fileAttrs.put("absolutePath", sourceFile.getAbsolutePath());
	fileAttrs.put("tree", root);
	fileAttrs.put("uid", root.getuid());
	// sandbox(this.root);
	jpFile.setAttributes(fileAttrs);
	return jpFile;
    }

    /**
     * Select all functions inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> function(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> functions = TomSelect.functions(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> functionsJPset = createJoinpoints(functions, JoinPoints.FUNCTION, root);
	if (functionsJPset.isEmpty())
	    return functionsJPset;
	Joinpoint jp = functionsJPset.get(0);
	jp.addAttribute("isMainFunction", true);
	jp.addAttribute("isSubFunction", false);
	for (int i = 1; i < functionsJPset.size(); i++) {
	    jp = functionsJPset.get(i);
	    jp.addAttribute("isMainFunction", false);
	    jp.addAttribute("isSubFunction", true);
	}
	return functionsJPset;
    }

    /**
     * Select all functions inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> loop(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> loops = TomSelect.loops(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> loopsJPset = createJoinpoints(loops, JoinPoints.LOOP, root);
	return loopsJPset;
    }

    /**
     * Select the body inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> body(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> bodies = TomSelect.body(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> bodiesJPset = createJoinpoints(bodies, JoinPoints.BODY, root);
	return bodiesJPset;
    }

    /**
     * Select the function input variables
     * 
     * @param parentJoinpoint
     *            the parent join point
     * @return List of input variables
     */
    public static List<Joinpoint> input(TomToken parentJoinpoint, TomToken root) {
	return var(parentJoinpoint, JoinPoints.INPUT, root);
    }

    /**
     * Select the function output variables
     * 
     * @param parentJoinpoint
     *            the parent join point
     * @return List of output variables
     */
    public static List<Joinpoint> output(TomToken parentJoinpoint, TomToken root) {
	return var(parentJoinpoint, JoinPoints.OUTPUT, root);
    }

    /**
     * Select all statements inside a MatlabToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> statement(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> statements = TomSelect.statement(parentJoinpoint);
	// System.out.println(statements);
	// If erro: TokenUtils.get...
	List<Joinpoint> statementJPset = createJoinpoints(statements, JoinPoints.STATEMENT, root);
	return statementJPset;
    }

    /**
     * Select the first statement inside a MatlabToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> first(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	TomToken firstStat = TomSelect.first(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> firstJPset = new ArrayList<Joinpoint>();
	if (firstStat == null)
	    return firstJPset;
	Joinpoint jp = createJoinpoint(firstStat, JoinPoints.STATEMENT, root);
	firstJPset.add(jp);
	return firstJPset;
    }

    /**
     * Select the first statement inside a MatlabToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> last(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	TomToken lastStat = TomSelect.last(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> lastJPset = new ArrayList<Joinpoint>();
	if (lastStat == null)
	    return lastJPset;
	Joinpoint jp = createJoinpoint(lastStat, JoinPoints.STATEMENT, root);
	lastJPset.add(jp);
	return lastJPset;
    }

    /**
     * Select the variable occurrences inside the join point parent
     * 
     * @param parentJoinpoint
     *            the parent join point
     * @return List of variable ocurrences
     */
    public static List<Joinpoint> var(TomToken parentJoinpoint, JoinPoints selectType, TomToken root) {
	// call tom strategy to get token reference
	List<TomToken> varTokens = TomSelect.vars(parentJoinpoint, selectType);
	// If erro: TokenUtils.get...
	List<Joinpoint> varJPset = createJoinpoints(varTokens, selectType, root);
	return varJPset;
    }

    /**
     * Select all ifs inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> If(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> ifs = TomSelect.Ifs(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> ifsJPset = createJoinpoints(ifs, JoinPoints.IF, root);
	return ifsJPset;
    }

    /**
     * Select then body inside an if
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> then(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> thens = TomSelect.then(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> thensJPset = createJoinpoints(thens, JoinPoints.BODY, root);
	return thensJPset;
    }

    /**
     * Select else body inside an if
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> Else(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> elses = TomSelect.Else(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> elsesJPset = createJoinpoints(elses, JoinPoints.BODY, root);
	return elsesJPset;
    }
    
    /**
     * Select all ifs inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> ElseIf(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> elseIfs = TomSelect.elseif(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> elseifsJPset = createJoinpoints(elseIfs, JoinPoints.ELSEIF, root);
	return elseifsJPset;
    }
    
    /**
     * Select all comments inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> comment(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> comments = TomSelect.comment(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> commentsJPset = createJoinpoints(comments, JoinPoints.COMMENT, root);
	return commentsJPset;
    }
    
    
    /**
     * Select all comments inside a TomToken
     * 
     * @param parentJoinpoint
     *            the parent joinpoint
     * @return
     */
    public static List<Joinpoint> section(TomToken parentJoinpoint, TomToken root) {

	// call tom strategy to get token reference
	List<TomToken> sections = TomSelect.section(parentJoinpoint);
	// If erro: TokenUtils.get...
	List<Joinpoint> sectionsJPset = createJoinpoints(sections, JoinPoints.SECTION, root);
	return sectionsJPset;
    }


    public static List<Joinpoint> createJoinpoints(List<TomToken> tokens, JoinPoints type,
	    TomToken root) {
	List<Joinpoint> jpSet = new ArrayList<Joinpoint>();
	for (TomToken token : tokens) {
	    Joinpoint jp = createJoinpoint(token, type, root);
	    jpSet.add(jp);
	}
	return jpSet;
    }

    public static Joinpoint createJoinpoint(TomToken token, JoinPoints type, TomToken root) {
	Joinpoint jp = new Joinpoint(token);
	Map<String, Object> attributes = TomAttributes.getAttributes(type, token, root);
	attributes.put("uid", token.getuid());
	jp.setAttributes(attributes);
	return jp;
    }
}
