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

package org.specs.MatlabIR.MatlabNode.nodes.root;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.MatlabIR.Exceptions.MatlabNodeException;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;

/**
 * Node representing a Matlab file.
 * 
 * <p>
 * All children of the node are MatlabUnits, either a single 'Script' child, several 'Function' children or a single
 * 'Classdef' child.
 * 
 * @author JoaoBispo
 *
 */
public abstract class FileNode extends MatlabNode {

    private final static String NO_FILE_NAME = "<NO_FILE>";

    // private final String filename;
    // private final String wholeFile;

    private final FileNodeContent fileContent;
    private final Map<String, MatlabUnitNode> unitNodesMap;

    /**
     * The string used when there is no filename (e.g., code came directly from a String).
     * 
     * @return
     */
    public static String getNoFilename() {
        return FileNode.NO_FILE_NAME;
    }

    public FileNode(FileNodeContent content, Collection<? extends MatlabNode> children) {

        // super(content.getFilename(), children);
        super(children);

        // this.filename = filename;
        // this.wholeFile = wholeFile;
        fileContent = content;
        unitNodesMap = buildUnitsMap();
    }

    protected FileNodeContent getFileContent() {
        return fileContent;
    }

    @Override
    public String getCode() {
        StringBuilder builder = new StringBuilder();

        for (MatlabNode child : getChildren()) {

            String childCode = child.getCode();

            builder.append(childCode);
            builder.append("\n");

        }

        return builder.toString();
    }

    public StringProvider getOriginalCode() {
        return fileContent.getOriginalCode();
    }

    public String getFilename() {
        // Check if it is not using paths as the filename.
        assert fileContent.getFilename().equals(new File((fileContent.getFilename())).getName());

        return fileContent.getFilename();
        // return filename;
    }

    public ScriptNode getScript() {
        return getScriptTry().get();

        /*
        if (numChildren() != 1) {
        throw new MatlabNodeException("Should have only one 'Script' child, found '" + numChildren(), this);
        }
        
        return getChild(ScriptNode.class, 0);
        */
    }

    public Optional<ScriptNode> getScriptTry() {
        // Check if it has children
        if (!hasChildren()) {
            return Optional.empty();
        }

        // Check if first child is a ScriptNode
        if (!(getChild(0) instanceof ScriptNode)) {
            return Optional.empty();
        }

        if (getNumChildren() != 1) {
            throw new MatlabNodeException("Should have only one 'Script' child, found '" + getNumChildren(), this);
        }

        return Optional.of(getChild(ScriptNode.class, 0));
    }

    public List<FunctionNode> getFunctions() {
        return getChildren(FunctionNode.class);
    }

    public FunctionNode getMainFunction() {
        return getMainFunctionTry().get();
        // return getChild(FunctionNode.class, 0);
    }

    public Optional<FunctionNode> getMainFunctionTry() {
        if (getUnits().isEmpty()) {
            return Optional.empty();
        }

        MatlabUnitNode firstUnit = getUnits().get(0);

        if (!(firstUnit instanceof FunctionNode)) {
            return Optional.empty();
        }

        return Optional.of((FunctionNode) firstUnit);
    }

    /**
     * 
     * @return
     */
    public List<MatlabUnitNode> getUnits() {
        return getChildren(MatlabUnitNode.class);
    }

    public Map<String, MatlabUnitNode> getUnitsMap() {
        return unitNodesMap;
    }

    /**
     * 
     * @param unitName
     * @return true if FileNode contains a unit with the given name
     */
    public boolean hasUnit(String unitName) {
        return unitNodesMap.containsKey(unitName);
    }

    /**
     * 
     * @param unitName
     * @return the unit mapped to the given name
     */
    public Optional<MatlabUnitNode> getUnit(String unitName) {
        return Optional.ofNullable(unitNodesMap.get(unitName));
    }

    /**
     * 
     * @return a list of the Statement or Block nodes from the first Function or from the Script
     */
    public List<StatementNode> getStatements() {
        if (!hasChildren()) {
            return Collections.emptyList();
        }

        // The children of the first node (Script of Function)
        return getUnits().get(0).getStatements();
    }

    /**
     * 
     * @return the name of the main unit
     */
    public abstract String getMainUnitName();

    /**
     * @return the functions
     */
    protected Map<String, MatlabUnitNode> buildUnitsMap() {
        if (getNumChildren() > 1) {
            throw new UnsupportedOperationException("Not implemented for for than one child");
        }

        Map<String, MatlabUnitNode> map = new HashMap<>();
        map.put(getMainUnitName(), getUnits().get(0));
        return map;
    }

    /*
    @Override
    public String toNodeString() {
    return getType().toString();
    
    }
    */

    public Optional<FunctionNode> getFunctionWithName(String name) {
        Preconditions.checkArgument(name != null);

        return getFunctions().stream()
                .filter(f -> f.getFunctionName().equals(name))
                .findFirst();
    }

    /**
     * 
     * @param file
     * @return true if filename ends with extension .m, or is equal to NO_FILE_NAME identifier
     */
    public static boolean isValidFilename(String filename) {
        if (filename.endsWith(".m")) {
            return true;
        }

        if (filename.equals(getNoFilename())) {
            return true;
        }

        return false;
    }

    @Override
    public String toContentString() {
        if (getFilename().equals(getNoFilename())) {
            return "";
        }

        return super.toContentString();
    }
}
