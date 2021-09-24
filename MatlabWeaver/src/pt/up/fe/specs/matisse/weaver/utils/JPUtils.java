/*
 * Copyright 2013 SPeCS.
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
package pt.up.fe.specs.matisse.weaver.utils;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;

public class JPUtils {

    /**
     * Get a JoinPoint parent of a certain type
     * 
     * @param child
     *            the child to find its ancestor
     * @param type
     *            the JoinPoint parent type to find
     * @return
     */
    public static <T> T getAncestorByType(AMWeaverJoinPoint child, Class<T> type) {

        AMWeaverJoinPoint parent = child;
        do {
            parent = parent.getParentImpl();
            // parent = (AMWeaverJoinPoint) parent.getAstParentImpl();
        } while (parent != null && !type.isInstance(parent));
        // } while (parent != null && !type.isInstance(parent.getNode()));
        return type.cast(parent);

        /*
        AMWeaverJoinPoint currentParent = child;
        while (currentParent != null) {
            if (type.isInstance(currentParent)) {
                return type.cast(currentParent);
            }
        
            currentParent = currentParent.getParentImpl();
        }
        
        return null;
        */
    }

    public static <T extends AMWeaverJoinPoint> T getAstAncestorByType(AMWeaverJoinPoint node, Class<T> type) {

        AJoinPoint currentParent = node.getAstParentImpl();
        // AJoinPoint currentParent = node;
        while (currentParent != null) {
            if (type.isInstance(currentParent)) {
                return type.cast(currentParent);
            }

            currentParent = currentParent.getAstParentImpl();
        }

        return null;
    }
}
