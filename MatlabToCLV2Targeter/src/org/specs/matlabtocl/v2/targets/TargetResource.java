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

package org.specs.matlabtocl.v2.targets;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum TargetResource implements ResourceProvider {
    DEFAULT("Default settings", "default"),
    GENERIC_CPU("Generic CPU", "cpu/GenericCpu"),
    AMD_PLATFORM_CPU("CPU (AMD Platform)", "cpu/AmdPlatformCpu"),
    INTEL_PLATFORM_CPU("CPU (Intel Platform)", "cpu/IntelPlatformCpu"),
    GENERIC_AMD_GPU("Generic AMD GPU", "gpu/amd/GenericAmd"),
    GENERIC_NVIDIA("Generic NVIDIA GPU", "gpu/nvidia/GenericNvidia"),
    AMD_NANO("AMD R9 Nano (Discrete GPU, 2015, HBM)", "gpu/amd/Nano"),
    AMD_SPECTRE("AMD Spectre (Integrated GPU, 2014)", "gpu/amd/Spectre"),
    AMD_TAHITI("AMD Tahiti (Discrete GPU, 2011)", "gpu/amd/Tahiti"),
    XILINX_FPGA_ADM_PCIE_KU3_2DDR("FPGA xilinx:adm-pcie-ku3:2ddr-xpr:3.2", "fpga/xilinx/adm-pcie-ku3_2ddr-xpr_3.2");

    private final String name;
    private final String resourcePath;

    TargetResource(String name, String resourcePath) {
        this.name = name;
        this.resourcePath = resourcePath;
    }

    public String getPlatformName() {
        return name;
    }

    @Override
    public String getResource() {
        return "platforms/" + resourcePath + ".lara";
    }

}
