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

package pt.up.fe.specs.matisse.weaver.joinpoints;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFile;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunction;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperator;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.joinpoints.enums.VariableType;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MArrayAccess;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MConventionalCall;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MExpression;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MFile;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MFunction;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MImplicitCall;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MOperator;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MScriptCall;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MVar;
import pt.up.fe.specs.matisse.weaver.utils.JPUtils;
import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class MJoinpointUtils {

    /**
     * Returns the fully qualified name of the function where the given >< is.
     * 
     * <p>
     * For instance, if the given << is inside a sub-function "B", which is part of function "A", returns ["A", "B"].
     * 
     * @return
     */
    public static List<String> getFunctionName(AMWeaverJoinPoint joinpoint) {

        // Get function parent
        AMWeaverJoinPoint funcParent = getParent(joinpoint, AFunction.class);
        /*
        AJoinPoint funcParent = joinpoint.getParent();
        while (funcParent != null && !(funcParent instanceof AFunction)) {
        funcParent = funcParent.getParent();
        }
        */

        // Could not find function
        if (funcParent == null) {
            return null;
        }

        // Get function name
        AFunction function = (AFunction) funcParent;
        String functionName = function.getNameImpl();

        // Get main function, go to AFile
        // Get function parent
        AMWeaverJoinPoint fileParent = getParent(funcParent, AFile.class);
        AFile file = (AFile) fileParent;

        // Get main function name
        String mainFunctionName = file.selectFunction().get(0).getNameImpl();

        List<String> key = SpecsFactory.newArrayList();
        key.add(mainFunctionName);

        // If function name is the same as the main function, return name
        if (mainFunctionName.equals(functionName)) {
            return key;
        }

        // Add sub-function name
        key.add(functionName);

        return key;
    }

    /**
     * Returns the first parent of jointpoint that implements the given class, or null if none is found.
     * 
     * @param joinpoint
     * @param parentClass
     * @return
     */
    public static AMWeaverJoinPoint getParent(AMWeaverJoinPoint joinpoint, Class<? extends AJoinPoint> parentClass) {

        // Get function parent
        AMWeaverJoinPoint funcParent = joinpoint.getParentImpl();
        while (funcParent != null && !(parentClass.isInstance(funcParent))) {
            funcParent = funcParent.getParentImpl();
        }

        // Could not find function
        if (funcParent == null) {
            return null;
        }

        return funcParent;
    }

    /**
     * 
     * This class exists because AJoinPoint does not have 'getAncestor' class
     * 
     * @param ancestorClass
     * @return
     */
    public static <T extends AJoinPoint> Optional<T> getAncestor(AMWeaverJoinPoint node, Class<T> ancestorClass) {
        AMWeaverJoinPoint parent = node.getParentImpl();

        if (parent == null) {
            return Optional.empty();
        }

        if (ancestorClass.isInstance(parent)) {
            return Optional.of(ancestorClass.cast(parent));
        }

        return getAncestor(parent, ancestorClass);
    }

    public static List<? extends AVar> getVars(AMWeaverJoinPoint parent, List<IdentifierNode> ids) {
        MFunction func = JPUtils.getAncestorByType(parent, MFunction.class);
        // Should be App
        MFile root = JPUtils.getAncestorByType(func, MFile.class);

        return ids.stream()
                // Check if identifier is from a variable
                .filter(node -> isVariable(func, root, node.getName()))
                .map(node -> new MVar(node, parent, VariableType.LOCAL))
                .collect(Collectors.toList());
    }

    public static List<? extends AOperator> getOperators(AMWeaverJoinPoint parent, List<OperatorNode> operators) {
        // MFunction func = JPUtils.getAncestorByType(parent, MFunction.class);
        // Should be App
        // MFile root = JPUtils.getAncestorByType(func, MFile.class);

        return operators.stream()
                .map(node -> new MOperator(node, parent))
                .collect(Collectors.toList());

    }

    public static boolean isFunctionCall(MFunction func, MFile root, String name) {
        return !root.containsVariable(func.getNameImpl(), name);
    }

    public static boolean isVariable(MFunction func, MFile root, String name) {
        // System.out.println("ROOT NULL: " + (root == null));
        // System.out.println("FUNC NULL: " + (func == null));
        return root.containsVariable(func.getNameImpl(), name);
    }

    public static AExpression fromExpression(MatlabNode node, AMWeaverJoinPoint parent) {
        if (node instanceof OperatorNode) {
            return new MOperator((OperatorNode) node, parent);
        }

        MFunction func = JPUtils.getAncestorByType(parent, MFunction.class);
        MFile root = JPUtils.getAncestorByType(parent, MFile.class);
        /*
        // AMWeaverJoinPoint nodeJp = MatlabJoinpoints.newJoinpoint(node, null);
        MatlabUnitNode unitNode = node.getAncestor(MatlabUnitNode.class);
        MFunction func = (MFunction) MatlabJoinpoints.newJoinpoint(unitNode, null);
        // MFunction func = JPUtils.getAstAncestorByType(nodeJp, MFunction.class);
        FileNode fileNode = node.getAncestor(FileNode.class);
        MFile root = (MFile) MatlabJoinpoints.newJoinpoint(fileNode, null);
        // MFile root = JPUtils.getAstAncestorByType(nodeJp, MFile.class);
        */
        if (node instanceof AccessCallNode) {
            AccessCallNode accessCallNode = (AccessCallNode) node;
            if (func != null && root != null && MJoinpointUtils.isFunctionCall(func, root, accessCallNode.getName())) {
                return new MConventionalCall(accessCallNode, parent);
            }
            return new MArrayAccess(accessCallNode, parent);
        }

        if (node instanceof CommandNode) {
            return new MScriptCall((CommandNode) node, parent);
        }

        if (node instanceof IdentifierNode) {
            IdentifierNode identifierNode = (IdentifierNode) node;

            if (func != null && root != null && MJoinpointUtils.isVariable(func, root, identifierNode.getName())) {
                return new MVar(identifierNode, parent, VariableType.LOCAL);
            } else if (!(identifierNode.getParent() instanceof AccessCallNode)
                    || identifierNode.getParent().getChild(0) != identifierNode) {
                return new MImplicitCall(identifierNode, parent);
            }
        }

        return new MExpression(node, parent);
    }

}
