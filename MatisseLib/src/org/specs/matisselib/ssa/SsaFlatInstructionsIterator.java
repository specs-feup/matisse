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

package org.specs.matisselib.ssa;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.specs.matisselib.ssa.instructions.SsaInstruction;

class SsaFlatInstructionsIterator<T extends SsaInstruction> implements Iterator<T> {

    Iterator<SsaBlock> blockIterator;
    ListIterator<SsaInstruction> instructionIterator;
    Class<T> cls;

    public SsaFlatInstructionsIterator(List<SsaBlock> blocks, Class<T> cls) {
        this.cls = cls;

        blockIterator = blocks.iterator();
    }

    @Override
    public boolean hasNext() {
        while (true) {
            while (instructionIterator == null || !instructionIterator.hasNext()) {
                if (!blockIterator.hasNext()) {
                    return false;
                }

                instructionIterator = blockIterator.next().getInstructions().listIterator();
            }

            while (instructionIterator.hasNext()) {
                if (cls.isInstance(instructionIterator.next())) {
                    instructionIterator.previous();
                    return true;
                }
            }

            if (!instructionIterator.hasNext()) {
                instructionIterator = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return (T) instructionIterator.next();
    }

}
