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

import java.util.Map;
import java.util.function.Function;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

import pt.up.fe.specs.util.classmap.ClassMap;
import pt.up.fe.specs.util.utilities.StringLines;

@FunctionalInterface
public interface XmlElementRule {

    /**
     * Returns the XML corresponding to the given node, using a set of rules for other nodes.
     * 
     * @param node
     * @param rules
     * @return
     */
    String getXml(MatlabNode node, ClassMap<MatlabNode, XmlElementRule> rules);

    default Function<MatlabNode, String> getName() {
	return MatlabXmlConverter.getDefaultName();
    }

    /**
     * Builds a map of strings from a node.
     * 
     * <p>
     * As default, builds an empty map.
     * 
     * @return
     */
    default Function<MatlabNode, Map<String, String>> getAttributes() {
	return MatlabXmlConverter.getDefaultAttributes();
    }

    default String getData(MatlabNode node, ClassMap<MatlabNode, XmlElementRule> rules) {
	// No children, return empty
	if (!node.hasChildren()) {
	    return getLeafData().apply(node);
	}

	// Add children nodes
	StringBuilder builder = new StringBuilder();

	// New line
	builder.append("\n");

	for (MatlabNode child : node.getChildren()) {
	    String childXml = rules.get(child).getXml(child, rules);

	    StringLines.getLines(childXml).stream()
		    .forEach(line -> builder.append(MatlabXmlConverter.getTab()).append(line).append("\n"));
	}

	return builder.toString();
    }

    /**
     * The data content when its a leaf node.
     * 
     * @return
     */
    default Function<MatlabNode, String> getLeafData() {
	return MatlabXmlConverter.getDefaultLeafData();
    }

    static class GenericXmlElementRule implements XmlElementRule {
	private final Function<MatlabNode, String> name;
	private final Function<MatlabNode, Map<String, String>> attributes;
	private final Function<MatlabNode, String> leafData;

	public GenericXmlElementRule(Function<MatlabNode, String> name,
		Function<MatlabNode, Map<String, String>> attributes,
		Function<MatlabNode, String> leafData) {

	    this.name = name;
	    this.attributes = attributes;
	    this.leafData = leafData;
	}

	@Override
	public Function<MatlabNode, String> getName() {
	    return name;
	}

	@Override
	public Function<MatlabNode, String> getLeafData() {
	    return leafData;
	}

	@Override
	public Function<MatlabNode, Map<String, String>> getAttributes() {
	    return attributes;
	}

	@Override
	public String getXml(MatlabNode node, ClassMap<MatlabNode, XmlElementRule> rules) {
	    return MatlabXmlConverter.getXml(this, node, rules);
	}
    }

    public static XmlElementRule newInstance() {
	return new GenericXmlElementRule(MatlabXmlConverter.getDefaultName(), MatlabXmlConverter.getDefaultAttributes(),
		MatlabXmlConverter.getDefaultLeafData());
    }

    /*
    public static XmlElementRule newInstance(Function<MatlabNode, String> leafData) {
    return new GenericXmlElementRule(XmlUtils.getDefaultName(), XmlUtils.getDefaultAttributes(), leafData);
    }
    */

    // EM is guaranteed to be a MatlabNode
    @SuppressWarnings("unchecked")
    public static <EM extends MatlabNode> XmlElementRule newInstance(Function<MatlabNode, String> name,
	    Function<EM, Map<String, String>> attributes, Function<MatlabNode, String> leafData) {

	return new GenericXmlElementRule(name, (Function<MatlabNode, Map<String, String>>) attributes, leafData);
    }

    // EM is guaranteed to be a MatlabNode
    @SuppressWarnings("unchecked")
    public static <EM extends MatlabNode> XmlElementRule newInstance(Function<EM, Map<String, String>> attributes) {

	return new GenericXmlElementRule(MatlabXmlConverter.getDefaultName(),
		(Function<MatlabNode, Map<String, String>>) attributes, MatlabXmlConverter.getDefaultLeafData());
    }
}
