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

package org.specs.matisselib.types;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.matisselib.functions.dynamiccell.CellFree;
import org.specs.matisselib.functions.dynamiccell.CellGet;
import org.specs.matisselib.functions.dynamiccell.CellNumDims;
import org.specs.matisselib.functions.dynamiccell.CellNumel;
import org.specs.matisselib.functions.dynamiccell.CreateCellFromDims;
import org.specs.matisselib.functions.dynamiccell.CreateFromCell;

public class DynamicCell implements Cell {
    private final DynamicCellType type;

    DynamicCell(DynamicCellType type) {
        this.type = type;
    }

    @Override
    public CellFunctions functions() {
        return new CellFunctions() {
            @Override
            public InstanceProvider createFromCell() {
                return CreateFromCell.getProvider();
            }

            @Override
            public InstanceProvider createFromDims() {
                return CreateCellFromDims.getProvider();
            }

            @Override
            public InstanceProvider copy() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public InstanceProvider numDims() {
                return CellNumDims.getProvider();
            }

            @Override
            public InstanceProvider numel() {
                return CellNumel.getProvider();
            }

            @Override
            public InstanceProvider get() {
                return CellGet.getProvider();
            }

            @Override
            public InstanceProvider free() {
                return CellFree.getProvider();
            }
        };
    }
}
