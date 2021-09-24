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

import java.math.BigDecimal;
import java.util.Map;

import com.google.common.collect.Maps;

public class BigDecimalFactory {

    /**
     * Statically Initialized Singleton
     */
    private Map<Integer, BigDecimal> maxSigned = Maps.newHashMap();
    private Map<Integer, BigDecimal> minSigned = Maps.newHashMap();
    private Map<Integer, BigDecimal> maxUnsigned = Maps.newHashMap();

    // private Map<String, BigDecimal> maxString;
    // private Map<String, BigDecimal> minString;

    private static BigDecimalFactory INSTANCE = new BigDecimalFactory();

    /**
     * Singleton instance which can be used as cache.
     * 
     * <p>
     * BigDecimal instances are immutable.
     * 
     * @return
     */
    public static BigDecimalFactory instance() {
	return INSTANCE;
    }

    private static void storeNumber(Map<Integer, BigDecimal> map, int bits, BigDecimal number) {

	map.put(bits, number);

    }

    private static BigDecimal getNumber(Map<Integer, BigDecimal> map, int bits) {
	if (map == null) {
	    return null;
	}

	return map.get(bits);
    }

    /*
        public static BigDecimal getIntegerMax(int bits, boolean isUnsigned) {
    
    	if (isUnsigned) {
    	    // 2^n - 1
    	    BigDecimal power = new BigDecimal(2).pow(bits);
    	    return power.subtract(BigDecimal.ONE);
    	}
    
    	// 2^(n-1) - 1
    	BigDecimal power = new BigDecimal(2).pow(bits - 1);
    	return power.subtract(BigDecimal.ONE);
        }
    */

    public BigDecimal getIntegerMax(int bits, boolean isUnsigned) {
	BigDecimal cached = getIntegerMaxCached(bits, isUnsigned);
	if (cached != null) {
	    return cached;
	}

	if (isUnsigned) {
	    // 2^n - 1
	    BigDecimal power = new BigDecimal(2).pow(bits);
	    cached = power.subtract(BigDecimal.ONE);
	} else {
	    // 2^(n-1) - 1
	    BigDecimal power = new BigDecimal(2).pow(bits - 1);
	    cached = power.subtract(BigDecimal.ONE);
	}

	setIntegerMaxCached(bits, isUnsigned, cached);
	return cached;
    }

    private void setIntegerMaxCached(int bits, boolean isUnsigned, BigDecimal cached) {
	if (isUnsigned) {
	    storeNumber(maxUnsigned, bits, cached);
	} else {
	    storeNumber(maxSigned, bits, cached);
	}

    }

    private BigDecimal getIntegerMaxCached(int bits, boolean isUnsigned) {
	if (isUnsigned) {
	    return getNumber(maxUnsigned, bits);
	}
	return getNumber(maxSigned, bits);
    }

    public BigDecimal getIntegerMin(int bits, boolean isUnsigned) {

	if (isUnsigned) {
	    return BigDecimal.ZERO;
	}

	BigDecimal cached = getNumber(minSigned, bits);
	if (cached != null) {
	    return cached;
	}

	// -2^(n-1)
	BigDecimal power = new BigDecimal(2).pow(bits - 1);
	cached = power.negate();

	storeNumber(minSigned, bits, cached);
	return cached;
    }

}
