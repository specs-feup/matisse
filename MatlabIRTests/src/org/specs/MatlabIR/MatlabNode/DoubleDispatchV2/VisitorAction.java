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

package org.specs.MatlabIR.MatlabNode.DoubleDispatchV2;

/**
 * An action that the visitor performs, where it receives an input I, and returns and output R.
 * 
 * @author JoaoBispo
 *
 * @param <I>
 *            the type of the input of the action
 * @param <R>
 *            the type of the output of the action
 */
public interface VisitorAction<I, R> {

    R apply(I node);

    @SuppressWarnings("unchecked")
    default Object applyUnsafe(Object node) {
	return apply((I) node);
    }

}
