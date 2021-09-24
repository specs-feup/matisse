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

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;

public class CLCodeGenUtils {
    public static final String HEADER_NAME = "matisse-cl.h";
    public static final String PROGRAM_SOURCE_CODE_NAME = "program.cl";
    public static final String HELPER_IMPLEMENTATION_NAME = "matisse_cl.c";

    public static CNode generateIsMasterThreadNode(SsaToOpenCLBuilderService builder) {
        List<CNode> localIds = builder.getLocalIdNodes();

        CNode isMasterThread = null;
        for (CNode localId : localIds) {
            List<CNode> isMasterThreadInDimensionArgs = Arrays.asList(
                    localId,
                    CNodeFactory.newCNumber(0));
            ProviderData isMasterThreadInDimensionData = builder.getCurrentProvider()
                    .createFromNodes(isMasterThreadInDimensionArgs);
            CNode isMasterThreadInDimension = CLBinaryOperator.EQUAL
                    .getCheckedInstance(isMasterThreadInDimensionData)
                    .newFunctionCall(isMasterThreadInDimensionArgs);

            if (isMasterThread == null) {
                isMasterThread = isMasterThreadInDimension;
            } else {
                List<CNode> isMasterThreadArgs = Arrays.asList(
                        isMasterThread,
                        isMasterThreadInDimension);
                ProviderData isMasterThreadData = builder.getCurrentProvider()
                        .createFromNodes(isMasterThreadArgs);
                isMasterThread = CLBinaryOperator.LOGICAL_AND
                        .getCheckedInstance(isMasterThreadData)
                        .newFunctionCall(isMasterThreadArgs);
            }
        }
        assert isMasterThread != null;

        return isMasterThread;
    }
}
