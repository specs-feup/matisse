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

package org.specs.CIR;

import static org.junit.Assert.*;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 *
 */
public class CirTestUtils {

    public static InstanceBuilder createHelper() {

        ProviderData data = ProviderData.newInstance(newDefaultSettings());
        GenericInstanceBuilder helper = new GenericInstanceBuilder(data);

        return helper;
    }

    public static ProviderData newDefaultProviderData() {
        return ProviderData.newInstance(newDefaultSettings());
    }

    /**
     * Creates a CIR setup with default values.
     * 
     * @return
     */
    public static DataStore newDefaultSettings() {
        return DataStore.newInstance("CIR_TESTS_SETUP");
    }

    public static void testException(Runnable runnable) {
        testException(runnable, null, false);
    }

    private static <T extends Exception> void testException(Runnable runnable, Class<T> aClass, boolean testException) {
        try {
            runnable.run();
            fail("Should throw exception");
        } catch (Exception e) {
            // Check if exception is of the same type
            if (testException) {
                assertTrue(aClass.isInstance(e));
            }
        }
    }

}
