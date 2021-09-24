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

package org.specs.MatlabProcessor.problematic;

import org.junit.Assert;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import junit.framework.TestCase;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class ProblematicTest extends TestCase {
    @Override
    protected void setUp() {
        SpecsSystem.programStandardInit();
        SpecsProperty.ShowStackTrace.applyProperty("true");
    }

    public void testProblematic() {
        for (ProblematicTestResource resource : ProblematicTestResource.values()) {
            try {
                testProblematic(resource.name(), resource.getResource(), resource.getResultResource());
                fail("Expected test to throw an exception");
            } catch (Exception e) {
                continue;
            }

        }
    }

    private static void testProblematic(String resourceName, String test, String resultResource) {
        System.out.println(resourceName);
        FileNode testToken = new MatlabParser().parse(() -> test);
        String result = SpecsIo.getResource(resultResource);

        ScriptNode script = testToken.getScript();
        StatementNode statement = script.getStatements().get(0);
        MatlabNode expression = statement.getChild(0);
        if (expression instanceof ExpressionNode) {
            System.err.println("Warning: Found expression node");
            expression = expression.getChild(0);
        }

        Assert.assertEquals(resourceName + ": " + result, resourceName + ": " + toAlternativeNotation(expression));
    }

    private static String toAlternativeNotation(MatlabNode node) {
        if (node instanceof OperatorNode) {
            OperatorNode operation = (OperatorNode) node;
            StringBuilder builder = new StringBuilder();
            builder.append(operation.getOp().getLiteral());
            builder.append('(');
            boolean isFirst = true;
            for (MatlabNode child : node.getChildren()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(toAlternativeNotation(child));
            }
            builder.append(')');

            return builder.toString();
        } else if (node instanceof ParenthesisNode || node instanceof ExpressionNode) {
            return toAlternativeNotation(node.getChild(0));
        }

        return node.getCode();
    }

}
