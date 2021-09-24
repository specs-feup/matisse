/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Program;

import java.util.Collection;
import java.util.List;

import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Types.FunctionName;
import org.specs.CIR.Utilities.AvoidableFunctionsData;
import org.specs.CIR.Utilities.Inlining.InliningData;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;

import com.google.common.collect.Lists;

import pt.up.fe.specs.guihelper.FieldType;
import pt.up.fe.specs.guihelper.SetupAccess;
import pt.up.fe.specs.guihelper.Base.SetupFieldEnum;
import pt.up.fe.specs.guihelper.BaseTypes.FieldValue;
import pt.up.fe.specs.guihelper.BaseTypes.SetupData;
import pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue;
import pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice;
import pt.up.fe.specs.util.SpecsEnums;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * @author Joao Bispo
 * 
 */
public enum ImplementationSetup implements SetupFieldEnum,DefaultValue,MultipleChoice {

    // UseMatrixImplementation(FieldType.multipleChoice),
    AllowDynamicAllocation(FieldType.bool),
    // InlineAllocatedArrayOperations(FieldType.bool),
    KindOfMemoryLayout(FieldType.multipleChoice),
    Inline(FieldType.multipleChoiceStringList),
    Avoid(FieldType.multipleChoiceStringList);
    // StaticArrayInitializationThreshold(FieldType.integer);

    private final FieldType fieldType;

    private static final List<String> INLINE_FUNCTIONS;
    private static final List<String> AVOID_FUNCTIONS;

    static {
	INLINE_FUNCTIONS = Lists.newArrayList();

	Collection<FunctionName> functionNames = MatlabToCOptionUtils.newInliningDataDefault().getSupportedFunctions();
	for (FunctionName functionName : functionNames) {
	    ImplementationSetup.INLINE_FUNCTIONS.add(functionName.name());
	}

	AVOID_FUNCTIONS = Lists.newArrayList();

	Collection<FunctionName> avoidableNames = MatlabToCOptionUtils.newAvoidStoreDefault().getSupportedFunctions();
	for (FunctionName functionName : avoidableNames) {
	    ImplementationSetup.AVOID_FUNCTIONS.add(functionName.name());
	}
    }

    public static ImplementationSettings newData(SetupData setupData) {
	SetupAccess setup = new SetupAccess(setupData);

	// MatrixImplementation impl = setup.getEnum(UseMatrixImplementation,
	// MatrixImplementation.class);
	boolean allowDynamicAllocation = setup.getBoolean(AllowDynamicAllocation);
	// boolean useDeclaredMatrixes = !allowDynamicAllocation;
	// boolean useAllocatedMatrixes = allowDynamicAllocation;
	// MatrixImplementation impl = MatrixImplementation.parse(setup.getString(UseMatrixImplementation));
	// boolean useDeclaredMatrixes = false;
	// boolean useAllocatedMatrixes = false;
	/*
		if (impl == MatrixImplementation.DECLARED) {
		    useDeclaredMatrixes = true;
		} else if (impl == MatrixImplementation.ALLOCATED) {
		    useAllocatedMatrixes = true;
		} else {
		    throw new RuntimeException("Implementation is neither static nor dynamic");
		}
	*/
	// boolean inlineArrayOperations = setup.getBoolean(InlineAllocatedArrayOperations);

	MemoryLayout disposition = setup.getEnum(KindOfMemoryLayout, MemoryLayout.class);

	InliningData inliningData = MatlabToCOptionUtils.newInliningDataDefault(false);
	inliningData.setInlineByName(setup.getListOfStrings(Inline));

	AvoidableFunctionsData avoidData = MatlabToCOptionUtils.newAvoidStoreDefault(false);
	avoidData.setAvoidByName(setup.getListOfStrings(Avoid));

	ImplementationSettings implSettings = new ImplementationSettings(allowDynamicAllocation, disposition,
		inliningData, avoidData);

	return implSettings;
    }

    /**
     * @param fieldType
     */
    private ImplementationSetup(FieldType fieldType) {
	this.fieldType = fieldType;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.Base.SetupFieldEnum#getType()
     */
    @Override
    public FieldType getType() {
	return fieldType;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.Base.SetupFieldEnum#getSetupName()
     */
    @Override
    public String getSetupName() {
	return "Implementation Settings";
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.DefaultValue#getDefaultValue()
     */
    @Override
    public FieldValue getDefaultValue() {
	switch (this) {
	case AllowDynamicAllocation:
	    return FieldValue.create(Boolean.TRUE, fieldType);
	// case UseMatrixImplementation:
	// return FieldValue.create(MatrixImplementation.DECLARED.name(), FieldType.multipleChoice);
	// case InlineAllocatedArrayOperations:
	// return FieldValue.create(Boolean.TRUE, FieldType.bool);
	case KindOfMemoryLayout:
	    return FieldValue.create(MemoryLayout.COLUMN_MAJOR.name(), FieldType.multipleChoice);
	case Inline:
	    return FieldValue.create(new StringList(ImplementationSetup.INLINE_FUNCTIONS),
		    FieldType.multipleChoiceStringList);
	default:
	    return null;
	}
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.guihelper.SetupFieldOptions.MultipleChoice#getChoices()
     */
    @Override
    public StringList getChoices() {
	// if (this == UseMatrixImplementation) {
	// return new StringList(EnumUtils.buildList(MatrixImplementation.values()));
	// }

	if (this == KindOfMemoryLayout) {
	    return new StringList(SpecsEnums.buildList(MemoryLayout.values()));
	}

	if (this == ImplementationSetup.Inline) {
	    return new StringList(ImplementationSetup.INLINE_FUNCTIONS);
	}

	if (this == ImplementationSetup.Avoid) {
	    return new StringList(ImplementationSetup.AVOID_FUNCTIONS);
	}

	return null;
    }

}
