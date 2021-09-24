/**
 * Copyright 2014 SPeCS.
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

package org.specs.CIRTypes.Types.StaticMatrix;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.Common.CreateAndCopyMatrixInstanceProvider;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.Data;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.Get;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.GetDim;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.NumDims;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.Numel;
import org.specs.CIRTypes.Types.StaticMatrix.Functions.SetStatic;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

/**
 * @author Joao Bispo
 * 
 */
public class StaticMatrixFunctions implements MatrixFunctions {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.AMatrixFunctions#numel()
     */
    @Override
    public InstanceProvider numel() {
        return (ProviderData data) -> new Numel(data).create();
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.MatrixFunctions#get()
     */
    @Override
    public InstanceProvider get() {
        return (ProviderData data) -> new Get(data).create();
    }

    @Override
    public InstanceProvider set() {
        return (ProviderData data) -> new SetStatic(data).create();
    }

    @Override
    public InstanceProvider data() {
        return (ProviderData data) -> new Data(data).create();
    }

    @Override
    public InstanceProvider createFromMatrix() {
        InstanceProvider provider = new InstanceProvider() {
            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                throw new UnsupportedOperationException("StaticMatrixType.createFromMatrix is a no-op.");
            }

            @Override
            public FunctionType getType(ProviderData data) {
                MatrixType matrixType = data.getInputType(MatrixType.class, 0);
                VariableType elementType = matrixType.matrix().getElementType();

                if (elementType.code().requiresExplicitInitialization()) {
                    throw new NotImplementedException(matrixType);
                }

                return FunctionTypeBuilder.newInline()
                        .addInput(matrixType)
                        .returning(matrixType)
                        .noOp()
                        .build();
            }
        };

        return provider;
    }

    @Override
    public InstanceProvider create() {

        InstanceProvider provider = new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                FunctionType type = getType(data);

                MatrixType matrixType = (MatrixType) data.getOutputType();
                String functionName = "create_static_" + matrixType.getSmallId();

                if (type.isNoOp()) {
                    return new InlinedInstance(type, functionName, tokens -> "// No-op instance");
                }

                StringBuilder body = new StringBuilder();
                body.append("for (int i = 0; i < ");
                body.append(matrixType.getTypeShape().getNumElements());
                body.append("; i++) {\n");

                ScalarType elementType = matrixType.matrix().getElementType();
                CNode node = CNodeFactory.newLiteral("(out[i])", elementType, PrecedenceLevel.Atom);

                CInstructionList instructions = elementType.code().getSafeDefaultDeclaration(node, data);
                for (CNode stmt : instructions.get()) {
                    body.append("\t");
                    body.append(stmt.getCode());
                }

                body.append("}\n\nreturn out;\n");

                LiteralInstance instance = new LiteralInstance(type, functionName, "lib/array_creators_dec",
                        body.toString());

                for (CNode stmt : instructions.get()) {
                    for (FunctionCallNode fcn : stmt.getDescendants(FunctionCallNode.class)) {
                        instance.addInstance(fcn.getFunctionInstance());
                    }
                }
                return instance;
            }

            public FunctionType getType(ProviderData data) {

                MatrixType matrixType = (MatrixType) data.getOutputType();
                if (matrixType == null) {
                    throw new RuntimeException("Must set ProviderData.getOutputType()");
                }

                VariableType elementType = matrixType.matrix().getElementType();

                FunctionTypeBuilder typeBuilder = FunctionTypeBuilder.newWithSingleOutputAsInput();

                List<String> inputNames = new ArrayList<>();
                for (int i = 0; i < data.getNumInputs(); ++i) {
                    inputNames.add("in" + i);
                }
                typeBuilder.addInputs(inputNames, data.getInputTypes());
                typeBuilder.addOutputAsInput("out", matrixType);

                boolean isExplicit = elementType.code().requiresExplicitInitialization();
                typeBuilder.noOpIf(!isExplicit);
                FunctionType type = typeBuilder.build();
                return type;
            }
        };

        return provider;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions#getDim()
     */
    @Override
    public InstanceProvider getDim() {
        return GetDim.getProvider();
    }

    @Override
    public InstanceProvider numDims() {
        return NumDims.getProvider();
    }

    @Override
    public InstanceProvider assign() {
        return new CreateAndCopyMatrixInstanceProvider();
    }
}
