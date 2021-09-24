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

package org.specs.MatlabIR.xmlwriter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.ExpressionNode;

import pt.up.fe.specs.util.classmap.ClassMap;
import pt.up.fe.specs.util.treenode.TreeNode;

public class MatlabXmlConverter {

    private final static String TAB = "   ";
    private final static ClassMap<MatlabNode, XmlElementRule> DEFAULT_RULES = new ClassMap<>();

    static {
	MatlabXmlConverter.DEFAULT_RULES.put(MatlabNode.class, XmlElementRule.newInstance());
	MatlabXmlConverter.DEFAULT_RULES.put(StatementNode.class, XmlElementRule.newInstance(node -> "Statement",
		getStatementAttributes(), MatlabXmlConverter.getDefaultLeafData()));
	MatlabXmlConverter.DEFAULT_RULES.put(OperatorNode.class, XmlElementRule.newInstance(getOperatorAttributes()));

	// Ignore ExpressionNodes, print single child of expression
	MatlabXmlConverter.DEFAULT_RULES.put(ExpressionNode.class,
		// (node, rules) -> XmlUtils.getXml(XmlElementRule.newInstance(), node.getChild(0), rules));
		(node, rules) -> rules.get(node.getChild(0)).getXml(node.getChild(0), rules));
    }

    static String getTab() {
	return MatlabXmlConverter.TAB;
    }

    static Function<MatlabNode, String> getDefaultLeafData() {
	// return node -> node.toContentString();
	return node -> node.getCode();
    }

    static Function<MatlabNode, String> getDefaultName() {
	return node -> {
	    String name = node.getClass().getSimpleName();

	    // Remove suffix Node
	    if (name.endsWith("Node")) {
		return name.substring(0, name.length() - 4);
	    }

	    return name;
	};
    }

    static <N extends TreeNode<?>> Function<N, Map<String, String>> getDefaultAttributes() {
	return node -> Collections.emptyMap();
    }

    public static ClassMap<MatlabNode, XmlElementRule> getDefaultRules() {
	return MatlabXmlConverter.DEFAULT_RULES.copy();
    }

    static Function<StatementNode, Map<String, String>> getStatementAttributes() {
	return statement -> {
	    Map<String, String> attributes = new LinkedHashMap<>();

	    attributes.put("type", statement.getStatementType().toString());
	    attributes.put("line", Integer.toString(statement.getLine()));

	    return attributes;
	};
    }

    static Function<OperatorNode, Map<String, String>> getOperatorAttributes() {
	return operator -> {
	    Map<String, String> attributes = new LinkedHashMap<>();

	    attributes.put("op", operator.getOp().toString());

	    return attributes;
	};
    }

    public static String toXml(MatlabNode node) {
	return MatlabXmlConverter.DEFAULT_RULES.get(node).getXml(node, MatlabXmlConverter.DEFAULT_RULES);
    }

    static String getXml(XmlElementRule rule, MatlabNode node, ClassMap<MatlabNode, XmlElementRule> rules) {
	StringBuilder builder = new StringBuilder();

	builder.append("<").append(rule.getName().apply(node));

	Map<String, String> attributes = rule.getAttributes().apply(node);
	if (!attributes.isEmpty()) {
	    String attributesString = attributes.entrySet().stream()
		    .map(entry -> entry.getKey() + "=\"" + entry.getValue() + "\"")
		    .collect(Collectors.joining(" ", " ", ""));

	    builder.append(attributesString);
	}

	builder.append(">");

	String nodeContent = rule.getData(node, rules);
	builder.append(nodeContent);
	// for (String line : StringLines.getLines(nodeContent)) {
	// builder.append(MatlabXmlConverter.TAB).append(line).append("\n");
	// }

	builder.append("</").append(rule.getName().apply(node)).append(">\n");

	return builder.toString();
    }
}
