/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.jOptions;

import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.CodeBuilder.MatToMatRules.FunctionCallOutliner;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.MatlabToC.Outlinable.OutlinableMap;
import org.specs.MatlabToC.Outlinable.OutlineCheck;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.unssa.VariableAllocator;
import org.specs.matisselib.unssa.allocators.EfficientVariableAllocator;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import pt.up.fe.specs.util.collections.HashSetString;
import pt.up.fe.specs.util.utilities.StringList;

/**
 * @author Joao Bispo
 * 
 */
public interface MatlabToCKeys {

    /**
     * TODO: Create new type in jOptions.Options for MultipleChoiceSeveral or EnumListMultiple, and replace here Def
     */
    // SPECIAL_STATEMENT_RULES(StringList.newOption("SPECIAL_STATEMENT_RULES",
    // EnumUtils.buildListToString(SpecialStatementRule.class))),

    /**
     * Maps the name of MATLAB functions to a list of FunctionBuilders, which will be added to the compilation table.
     * The builders specified in the map take precedence over previous builders.
     * 
     * <p>
     * Returns a {@link org.specs.MatlabToC.jOptions.BuildersMap}.
     */
    public static final DataKey<BuildersMap> MATLAB_BUILDERS = KeyFactory.object("builders_map", BuildersMap.class)
            .setDefault(() -> new BuildersMap());

    /**
     * When an InstanceProvider is not found for a given MATLAB function, creates a stub instead of stoping the
     * compilation.
     */
    public static final DataKey<Boolean> ENABLE_STUBS = KeyFactory.bool("enable_stubs");

    /**
     * Enables a new, alternative compilation path, the pass system.
     */
    public static final DataKey<Boolean> USE_PASS_SYSTEM = KeyFactory.bool("use_pass_system");

    /**
     * Pragma to use on loops with no carried dependencies.
     */
    public static final DataKey<IvdepType> IVDEP_TYPE = KeyFactory.enumeration("ivdep_type", IvdepType.class);
    /**
     * A file containing the optimizations to apply, when using the new pass system.
     */
    public static final DataKey<String> CUSTOM_RECIPE = KeyFactory.string("custom_recipe");

    /**
     * A file containing the optimizations to apply, when using the new pass system.
     */
    public static final DataKey<String> CUSTOM_PRE_TYPE_SSA_RECIPE = KeyFactory
            .string("custom_pre_type_ssa_recipe", "");

    /**
     * A Runnable step to execute after a successful compilation.
     */
    public static final DataKey<PostCodeGenAction> POST_CODEGEN_STEP = KeyFactory.object("post compilation step",
            PostCodeGenAction.class).setDefault(() -> PostCodeGenAction.NO_OP);

    /**
     * A HashSetString containing the names of MATLAB functions that are considered element wise.
     */
    public static final DataKey<HashSetString> ELEMENT_WISE_FUNCTIONS = KeyFactory.object("element_wise_functions",
            HashSetString.class).setDefault(() -> new HashSetString());

    /**
     * A HashSetString containing the names of optimizations performed by MATISSE.
     * 
     * TODO: Replace with an EnumSet-like object, which specializes to MatisseOptimization.
     */
    public static final DataKey<HashSetString> MATISSE_OPTIMIZATIONS = KeyFactory.object("matisse_optimizations",
            HashSetString.class).setDefault(() -> new HashSetString());

    // public static final DataKey<EnumList<MatisseOptimization>> MATISSE_OPTIMIZATIONS_V2 = KeyFactory
    // .enumList("matisse_optimizations_v2", MatisseOptimization.class);

    /**
     * An Integer indicating, from which number of elements should matrix multiplication with BLAS be used.
     */
    public static final DataKey<Integer> BLAS_THRESHOLD = KeyFactory.integer("blas_threshold", 256);

    /**
     * A String representing the (preferably) absolute path to the folder of the BLAS library, to add during
     * compilation.
     */
    public static final DataKey<String> BLAS_LIB_FOLDER = KeyFactory.string("blas_lib_folder");

    /**
     * A String representing the (preferably) absolute path to the folder of the BLAS includes, to add during
     * compilation.
     */
    public static final DataKey<String> BLAS_INCLUDE_FOLDER = KeyFactory.string("blas_include_folder");

    public static final DataKey<InferenceRuleList> TYPE_INFERENCE_RULES = KeyFactory.object("type_inference_rules",
            InferenceRuleList.class)
            .setDefault(() -> new InferenceRuleList(TypeInferencePass.BASE_TYPE_INFERENCE_RULES));

    public static final DataKey<StringList> CUSTOM_PRE_TYPE_SSA_RECIPE_PATHS = KeyFactory
            .stringList("custom_recipe_paths");

    public static final DataKey<StringList> CUSTOM_POST_TYPE_RECIPE_PATHS = KeyFactory
            .stringList("custom_post_type_recipe_paths");

    public static final DataKey<DataView> ADDITIONAL_SERVICES = KeyFactory
            .object("additional_services", DataView.class).setDefault(() -> DataView.empty());

    public static final DataKey<SsaToCRuleList> ADDITIONAL_SSA_TO_C_RULES = KeyFactory
            .object("additional_ssa_to_c_rules", SsaToCRuleList.class).setDefault(() -> SsaToCRuleList.EMPTY);

    public static final DataKey<Boolean> ENABLE_Z3 = KeyFactory.bool("enable_z3");

    public static final DataKey<VariableAllocator> CUSTOM_VARIABLE_ALLOCATOR = KeyFactory
            .object("custom_allocator", VariableAllocator.class)
            .setDefault(() -> new EfficientVariableAllocator());

    /**
     * Checks to determine if a given AccessCall should be outlined or not.
     */
    public static final DataKey<OutlinableMap> OUTLINE_CHECKS = KeyFactory.object("outline_checks",
            OutlinableMap.class).setDefault(() -> FunctionCallOutliner.getDefaultRules());

    /**
     * TODO: Deprecate method after MATISSE_OPTIMIZATIONS uses an EnumSet-like object.
     * 
     * @param settings
     * @param opt
     * @return
     */
    public static boolean isActive(DataStore settings, MatisseOptimization opt) {
        return settings.get(MatlabToCKeys.MATISSE_OPTIMIZATIONS).contains(opt.getName());
    }

    public static boolean isOutlinable(DataStore settings, AccessCallNode accessCall, MatlabToCFunctionData data) {
        OutlinableMap outlines = settings.get(MatlabToCKeys.OUTLINE_CHECKS);

        Optional<OutlineCheck> rule = outlines.getRule(accessCall.getName());

        // Check if rule exists
        if (!rule.isPresent()) {
            return false;
        }

        // Apply rule
        return rule.get().isOutlinable(accessCall, data);
    }

    /**
     * 
     * The default floating point type to be used (e.g., single, double...).
     * 
     * <p>
     * Returns a {@link org.specs.CIR.Types.VariableType}.
     */
    // DEFAULT_REAL(OptionFactory.newOption("default_float", VariableType.class,
    // NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64)));
}
