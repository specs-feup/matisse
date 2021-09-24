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

import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.CommandNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.CommentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseIfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.ElseSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.IfSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.LoopSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums.AFunctionFtypeEnum;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MArrayAccess;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MAssignment;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MComment;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MConventionalCall;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MElse;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MElseif;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MFunction;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MFunctionHeader;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MIf;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MLoop;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MOperator;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MScriptCall;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MStatement;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.FunctionAdapter;
import pt.up.fe.specs.matisse.weaver.utils.functionadapter.FunctionNodeAdapter;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.classmap.BiFunctionClassMap;

public class MatlabJoinpoints {

    private static final BiFunctionClassMap<MatlabNode, AMWeaverJoinPoint, AMWeaverJoinPoint> JOINPOINT_FACTORY;
    static {
        JOINPOINT_FACTORY = new BiFunctionClassMap<>();
        JOINPOINT_FACTORY.put(AccessCallNode.class, MArrayAccess::new);
        JOINPOINT_FACTORY.put(AssignmentSt.class, MAssignment::new);
        JOINPOINT_FACTORY.put(CommentSt.class, MComment::new);
        JOINPOINT_FACTORY.put(AccessCallNode.class, MConventionalCall::new);
        JOINPOINT_FACTORY.put(ElseIfSt.class, MElseif::new);
        JOINPOINT_FACTORY.put(FunctionNode.class, MatlabJoinpoints::functionFactory);
        JOINPOINT_FACTORY.put(FunctionDeclarationSt.class, MFunctionHeader::new);
        JOINPOINT_FACTORY.put(IfSt.class, MIf::new);
        JOINPOINT_FACTORY.put(ElseSt.class, MElse::new);
        JOINPOINT_FACTORY.put(LoopSt.class, MLoop::new);
        JOINPOINT_FACTORY.put(OperatorNode.class, MOperator::new);
        JOINPOINT_FACTORY.put(CommandNode.class, MScriptCall::new);
        JOINPOINT_FACTORY.put(StatementNode.class, MStatement::new);
        // JOINPOINT_FACTORY.put(FunctionFileNode.class, MatlabJoinpoints::fileFactory);
        JOINPOINT_FACTORY.put(MatlabNode.class, MatlabJoinpoints::defaultFactory);
    }

    private static AMWeaverJoinPoint functionFactory(FunctionNode node, AMWeaverJoinPoint parent) {
        FunctionAdapter adapter = new FunctionNodeAdapter(node);
        AFunctionFtypeEnum type = node.isMainFunction() ? AFunctionFtypeEnum.MAIN_FUNCTION
                : AFunctionFtypeEnum.NESTED_FUNCTION;

        return new MFunction(adapter, parent, type);
    }

    /*
    private static MFile fileFactory(FunctionFileNode node, AMWeaverJoinPoint parent) {
        System.out.println("FILENAME:" + node.getFilename());
    
        MFile mfile = new MFile(new File(node.getFilename()), parent);
        System.out.println("MFILE:" + mfile);
        // mfile.setMatlabRoot(node);
        // return mfile;
        return mfile;
    }
    */

    private static AMWeaverJoinPoint defaultFactory(MatlabNode node, AMWeaverJoinPoint parent) {
        // Check that it is not a statement
        if (node instanceof StatementNode || node instanceof FileNode) {
            SpecsLogs.msgInfo("Not supported for nodes of class '" + node.getClass().getSimpleName() + "'");
            return null;
        }

        return MJoinpointUtils.fromExpression(node, parent);

    }

    public static AMWeaverJoinPoint newJoinpoint(MatlabNode node, AMWeaverJoinPoint parent) {
        return JOINPOINT_FACTORY.apply(node, parent);
    }

    public static <T extends AJoinPoint> T newJoinpoint(MatlabNode node, AMWeaverJoinPoint parent,
            Class<T> targetClass) {
        return targetClass.cast(newJoinpoint(node, parent));
    }

    /**
     * The first ancestor (including self) of the given type.
     * 
     * @param joinpointClass
     * @return
     */

    public static <T extends AJoinPoint> Optional<T> getAncestorandSelf(AJoinPoint joinpoint, Class<T> joinpointClass) {
        AJoinPoint currentJp = joinpoint;

        if (joinpointClass.isInstance(currentJp)) {
            return Optional.of(joinpointClass.cast(currentJp));
        }

        while (currentJp.getHasParentImpl()) {
            currentJp = currentJp.getParentImpl();

            if (joinpointClass.isInstance(currentJp)) {
                return Optional.of(joinpointClass.cast(currentJp));
            }
        }

        return Optional.empty();
    }

}
