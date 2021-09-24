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

package org.specs.CIR.Utilities;

import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.ATypes.Matrix.MatrixNodes;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Utility class to given easier access to CNode factories in the CIR package.
 * 
 * @author JoaoBispo
 *
 */
public class CirNodes {

    private final DataStore setup;
    private MatrixNodes matrixNodes;
    private CNodeFactory factory;

    public CirNodes(DataStore setup) {
        this.setup = setup;
        matrixNodes = null;
        factory = null;
    }

    public CNodeFactory base() {
        if (factory == null) {
            factory = new CNodeFactory(setup);
        }

        return factory;
    }

    public MatrixNodes matrix() {
        if (matrixNodes == null) {
            matrixNodes = new MatrixNodes(setup);
        }

        return matrixNodes;
    }

}
