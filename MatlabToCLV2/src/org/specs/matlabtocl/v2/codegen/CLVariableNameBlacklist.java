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

package org.specs.matlabtocl.v2.codegen;

import java.util.Set;

import org.specs.matlabtocl.v2.types.kernel.AddressSpace;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public class CLVariableNameBlacklist {
    private CLVariableNameBlacklist() {
    }

    public static void addAdditionalOpenCLKeywords(Set<String> blacklist) {

	for (CLNativeType type : CLNativeType.values()) {
	    String name = type.code().getSimpleType();

	    if (type.getBits().orElse(-1) != 1) {
		blacklist.add(name + "[0-9]*");
	    } else {
		blacklist.add(name);
	    }
	}

	blacklist.add("image2d_t");
	blacklist.add("image3d_t");
	blacklist.add("sampler_t");
	blacklist.add("event_t");

	blacklist.add("quad[0-9]*");
	blacklist.add("complex");
	blacklist.add("imaginary");
	blacklist.add("float[0-9]+x[0-9]+");
	blacklist.add("double[0-9]+x[0-9]+");

	for (AddressSpace addressSpace : AddressSpace.values()) {
	    blacklist.add(addressSpace.getKeyword());
	    blacklist.add("__" + addressSpace.getKeyword());
	}

	blacklist.add("__kernel");
	blacklist.add("kernel");

	blacklist.add("__read_only");
	blacklist.add("read_only");
	blacklist.add("__write_only");
	blacklist.add("write_only");
	blacklist.add("__read_write");
	blacklist.add("read_write");
    }

    public static void addOpenCLFunctions(Set<String> blacklist) {
	blacklist.add("get_work_dim");
	blacklist.add("get_global_size");
	blacklist.add("get_global_id");
	blacklist.add("get_local_size");
	blacklist.add("get_local_id");
	blacklist.add("get_num_groups");
	blacklist.add("get_group_id");
	blacklist.add("get_global_offset");

	blacklist.add("acos");
	blacklist.add("acosh");
	blacklist.add("acospi");
	blacklist.add("asin");
	blacklist.add("asinh");
	blacklist.add("asinpi");
	blacklist.add("atan");
	blacklist.add("atan2");
	blacklist.add("atanh");
	blacklist.add("atanpi");
	blacklist.add("atan2pi");
	blacklist.add("cbrt");
	blacklist.add("ceil");
	blacklist.add("copysign");
	blacklist.add("cos");
	blacklist.add("cosh");
	blacklist.add("cospi");
	blacklist.add("erfc");
	blacklist.add("erf");
	blacklist.add("exp");
	blacklist.add("exp2");
	blacklist.add("exp10");
	blacklist.add("expm1");
	blacklist.add("fabs");
	blacklist.add("fdim");
	blacklist.add("floor");
	blacklist.add("fma");
	blacklist.add("fmax");
	blacklist.add("fmin");
	blacklist.add("fmod");
	blacklist.add("fract");
	blacklist.add("frexp");
	blacklist.add("hypot");
	blacklist.add("ilogb");
	blacklist.add("ldexp");
	blacklist.add("lgamma");
	blacklist.add("lgamma_r");
	blacklist.add("log");
	blacklist.add("log2");
	blacklist.add("log10");
	blacklist.add("log1p");
	blacklist.add("logb");
	blacklist.add("mad");
	blacklist.add("maxmag");
	blacklist.add("minmag");
	blacklist.add("modf");
	blacklist.add("nan");
	blacklist.add("nextafter");
	blacklist.add("pow");
	blacklist.add("pown");
	blacklist.add("powr");
	blacklist.add("remainder");
	blacklist.add("remquo");
	blacklist.add("rint");
	blacklist.add("rootn");
	blacklist.add("round");
	blacklist.add("rsqrt");
	blacklist.add("sin");
	blacklist.add("sincos");
	blacklist.add("sinh");
	blacklist.add("sinpi");
	blacklist.add("sqrt");
	blacklist.add("tan");
	blacklist.add("tanh");
	blacklist.add("tanpi");
	blacklist.add("tgamma");
	blacklist.add("trunc");

	blacklist.add("half_cos");
	blacklist.add("half_divide");
	blacklist.add("half_exp");
	blacklist.add("half_exp2");
	blacklist.add("half_exp10");
	blacklist.add("half_log");
	blacklist.add("half_log2");
	blacklist.add("half_log10");
	blacklist.add("half_powr");
	blacklist.add("half_recip");
	blacklist.add("half_rsqrt");
	blacklist.add("half_sin");
	blacklist.add("half_sqrt");
	blacklist.add("half_sin");
	blacklist.add("half_sqrt");
	blacklist.add("half_tan");
	blacklist.add("native_cos");
	blacklist.add("native_divide");
	blacklist.add("native_exp");
	blacklist.add("native_exp2");
	blacklist.add("native_exp10");
	blacklist.add("native_log");
	blacklist.add("native_log2");
	blacklist.add("native_log10");
	blacklist.add("native_powr");
	blacklist.add("native_recip");
	blacklist.add("native_rsqrt");
	blacklist.add("native_sin");
	blacklist.add("native_sqrt");
	blacklist.add("native_tan");

	blacklist.add("MAXFLOAT");
	blacklist.add("HUGE_VALF");
	blacklist.add("INFINITY");
	blacklist.add("NAN");

	// TODO: Section 6.11.2.1
	// TODO: Section 6.11.3-7

	blacklist.add("barrier");
	blacklist.add("CLK_LOCAL_MEM_FENCE");
	blacklist.add("CLK_GLOBAL_MEM_FENCE");

	blacklist.add("mem_fence");
	blacklist.add("read_mem_fence");
	blacklist.add("write_mem_fence");

	// TODO: Section 6.11.10-13

    }
}
