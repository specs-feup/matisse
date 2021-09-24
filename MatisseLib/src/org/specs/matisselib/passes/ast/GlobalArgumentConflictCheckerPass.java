package org.specs.matisselib.passes.ast;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.passmanager.PassManager;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class GlobalArgumentConflictCheckerPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode node, DataStore data) {
        if (node instanceof FileNode) {
            for (MatlabNode child : node.getChildren()) {
                if (child instanceof FunctionNode) {
                    checkFunction((FunctionNode) child, data);
                }
            }
        } else if (node instanceof FunctionNode) {
            checkFunction((FunctionNode) node, data);
        } else {
            throw new UnsupportedOperationException(
                    "Can't apply GlobalArgumentConflictCheckerPass pass to " + node.getNodeName());
        }

        return node;
    }

    private void checkFunction(FunctionNode node, DataStore data) {
        List<String> globals = node.getDescendantsStream()
                .filter(GlobalSt.class::isInstance)
                .map(GlobalSt.class::cast)
                .flatMap(globalSt -> globalSt.getIdentifiers().stream())
                .distinct()
                .collect(Collectors.toList());

        for (MatlabNode child : node.getChildren()) {
            if (!(child instanceof IdentifierNode)) {
                continue;
            }
            IdentifierNode input = (IdentifierNode) child;
            if (globals.contains(input.getName())) {
                throw data.get(PassManager.NODE_REPORTING).emitError(input,
                        PassMessage.NOT_SUPPORTED,
                        "MATISSE does not support functions with globals and inputs with the same name.");
            }
        }
        for (MatlabNode output : node.getOutputs()) {
            if (output instanceof IdentifierNode && globals.contains(((IdentifierNode) output).getName())) {
                throw data.get(PassManager.NODE_REPORTING).emitError(output,
                        PassMessage.NOT_SUPPORTED,
                        "MATISSE does not support functions with globals and outputs with the same name.");
            }
        }
    }

}
