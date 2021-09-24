package org.specs.matisselib.passes.ast;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabStringNode;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class StringConversionPass extends AMatlabNodePass {

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
        MatlabNodeIterator it = rootNode.getChildrenIterator();
        while (it.hasNext()) {
            it.set(apply(it.next(), data));
        }

        if (rootNode instanceof MatlabStringNode) {
            MatlabStringNode stringNode = (MatlabStringNode) rootNode;
            return MatlabNodeFactory.newSimpleAccessCall("string",
                    MatlabNodeFactory.newCharArray(stringNode.getString()));
        }

        return rootNode;
    }

}
