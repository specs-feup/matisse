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

package org.specs.MatlabPreprocessor;

import java.util.Arrays;

import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabIR.xmlwriter.MatlabXmlConverter;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

public class ToXmlTester {

    @Test
    public void test() {

        MatlabNode number = MatlabNodeFactory.newNumber(2);
        MatlabNode operator = MatlabNodeFactory.newOperator("+", number, number);
        MatlabNode expr = new ExpressionNode(null, Arrays.asList(operator));
        System.out.println("TEST:" + MatlabXmlConverter.toXml(expr));
        System.out.println(
                MatlabXmlConverter.getDefaultRules().get(expr).getXml(expr, MatlabXmlConverter.getDefaultRules()));
        MatlabNode node = (new MatlabParser().parse("a = b + c"));
        System.out.println("TREE:\n" + node);
        // System.out.println(XmlUtils.toXml(node));
    }

}
