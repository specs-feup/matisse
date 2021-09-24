package org.specs.matisselib.passes.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.CellNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class CellReplacementPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
        MatlabNodeIterator it = rootNode.getChildrenIterator();
        while (it.hasNext()) {
            it.set(apply(it.next(), data));
        }

        if (rootNode instanceof CellNode) {
            CellNode cellNode = (CellNode) rootNode;

            List<RowNode> rows = cellNode.getRows();
            if (rows.size() > 1) {
                List<MatlabNode> individualRows = new ArrayList<>();
                for (RowNode row : rows) {
                    individualRows.add(MatlabNodeFactory.newCell(Arrays.asList(row)));
                }
                return MatlabNodeFactory.newSimpleAccessCall("vertcat", individualRows);
            }
        }

        return rootNode;
    }

}
