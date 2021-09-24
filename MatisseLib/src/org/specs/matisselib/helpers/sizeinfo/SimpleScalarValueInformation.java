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

package org.specs.matisselib.helpers.sizeinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;

import com.google.common.base.Preconditions;

public class SimpleScalarValueInformation extends ScalarValueInformation {
    private static final class ScalarGroup {
	private final List<String> variablesInGroup = new ArrayList<>();
	private Double constant = null;
	private final Function<String, Optional<VariableType>> typeGetter;

	ScalarGroup(String name, Function<String, Optional<VariableType>> typeGetter) {
	    this.typeGetter = typeGetter;

	    add(name);
	}

	ScalarGroup(ScalarGroup group) {
	    this.variablesInGroup.addAll(group.variablesInGroup);
	    this.constant = group.constant;
	    this.typeGetter = group.typeGetter;
	}

	boolean contains(String variableName) {
	    return this.variablesInGroup.contains(variableName);
	}

	void add(String variableName) {
	    this.variablesInGroup.add(variableName);

	    this.typeGetter.apply(variableName)
		    .filter(ScalarUtils::isScalar)
		    .filter(ScalarUtils::hasConstant)
		    .map(ScalarUtils::getConstant)
		    .ifPresent(number -> {

			setConstant(number.doubleValue());

		    });
	}

	void setConstant(Double number) {
	    if (this.constant == null) {
		this.constant = number;
	    } else if (this.constant.doubleValue() != number.doubleValue()) {
		System.err.println("Adding constant " + number + " to group " + this.variablesInGroup
			+ ", but group already had different constant " + this.constant);
	    }
	}

	@Override
	public String toString() {
	    return this.variablesInGroup.toString();
	}
    }

    private final List<ScalarGroup> scalarGroups = new ArrayList<>();
    private final Function<String, Optional<VariableType>> typeGetter;

    public SimpleScalarValueInformation(Function<String, Optional<VariableType>> typeGetter) {
	this.typeGetter = typeGetter;
    }

    @Override
    public void close() {
	// Nothing needed
    }

    @Override
    public SimpleScalarValueInformation copy() {
	SimpleScalarValueInformation other = new SimpleScalarValueInformation(this.typeGetter);
	for (ScalarGroup group : this.scalarGroups) {
	    other.scalarGroups.add(new ScalarGroup(group));
	}

	return other;
    }

    private int getOrMakeScalarGroup(String name) {
	for (int i = 0; i < this.scalarGroups.size(); ++i) {
	    if (this.scalarGroups.get(i).contains(name)) {
		return i;
	    }
	}

	ScalarGroup newGroup = new ScalarGroup(name, this.typeGetter);
	this.scalarGroups.add(newGroup);
	return this.scalarGroups.size() - 1;
    }

    @Override
    public void buildScalarCopy(String outScalar, String inScalar) {
	Preconditions.checkArgument(outScalar != null);
	Preconditions.checkArgument(inScalar != null);

	int group = getOrMakeScalarGroup(inScalar);
	this.scalarGroups.get(group).add(outScalar);
    }

    @Override
    public void addAlias(String oldValue, String newValue) {
	for (ScalarGroup group : this.scalarGroups) {
	    if (group.variablesInGroup.contains(oldValue)) {
		group.variablesInGroup.add(newValue);
		return;
	    }
	}

	ScalarGroup newGroup = new ScalarGroup(oldValue, this.typeGetter);
	newGroup.add(newValue);
	this.scalarGroups.add(newGroup);
    }

    @Override
    public boolean areSameValue(String v1, String v2) {
	if (v1.equals(v2)) {
	    return true;
	}

	int g1 = getOrMakeScalarGroup(v1);
	int g2 = getOrMakeScalarGroup(v2);

	if (g1 == g2) {
	    return true;
	}

	Double const1 = this.scalarGroups.get(g1).constant;
	Double const2 = this.scalarGroups.get(g2).constant;

	if (const1 != null) {
	    return const1.equals(const2);
	}

	return false;
    }

    @Override
    public boolean isKnownLessOrEqualTo(String v1, String v2) {
	// FIXME

	return areSameValue(v1, v2);
    }

    @Override
    public void setUpTo(String value, String maximum) {
	// FIXME

	addAlias(maximum, value);
    }

    @Override
    public void setAtLeast(String value, String minimum) {
    }

    @Override
    public String toString() {
	return "[SimpleScalarValueInformation " + this.scalarGroups + "]";
    }

    @Override
    public void specifyConstant(String var, double value) {
	int groupId = getOrMakeScalarGroup(var);
	ScalarGroup group = this.scalarGroups.get(groupId);
	if (group.constant == null) {
	    group.constant = value;
	} else {
	    assert group.constant == value : "Redefining constant of value " + group.constant + " to different value "
		    + value + ", for variable " + var;
	}
    }

    @Override
    public boolean isKnownLessThan(String v1, String v2) {
	Optional<Double> value1 = getConstantValue(v1);
	Optional<Double> value2 = getConstantValue(v2);

	return value1.isPresent() &&
		value2.isPresent() &&
		value1.get() < value2.get();
    }

    @Override
    public boolean isKnownEqual(String var, int i) {
	Optional<Double> value = getConstantValue(var);

	return value.map(v -> v == i)
		.orElse(false);
    }

    private Optional<Double> getConstantValue(String var) {
	Optional<Double> value = this.typeGetter.apply(var)
		.filter(ScalarType.class::isInstance)
		.map(ScalarType.class::cast)
		.map(scalarType -> scalarType.scalar())
		.filter(scalar -> scalar.hasConstant())
		.map(scalar -> scalar.getConstant().doubleValue());
	return value;
    }

    @Override
    public boolean isKnownNotEqual(String var, int i) {
	Optional<Double> value = getConstantValue(var);

	return value.map(v -> v != i)
		.orElse(false);
    }

    @Override
    public boolean isKnownPositive(String var) {
	Optional<Double> value = getConstantValue(var);

	return value.map(v -> v > 0)
		.orElse(false);
    }

    @Override
    public boolean isKnownNegative(String var) {
	Optional<Double> value = getConstantValue(var);

	return value.map(v -> v < 0)
		.orElse(false);
    }
}
