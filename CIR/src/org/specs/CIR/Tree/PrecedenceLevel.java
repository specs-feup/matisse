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

package org.specs.CIR.Tree;

import com.google.common.base.Preconditions;

public enum PrecedenceLevel {
    Atom(false),

    Level1(false),
    Level2(true),
    Level3(false),
    Level4(false),
    Level5(false),
    Level6(false),
    Level7(false),
    Level8(false),
    Level9(false),
    Level10(false),
    Level11(false),
    Level12(false),
    Level13(true),
    Level14(true),
    Level15(false),

    Unspecified(true);

    /**
     * <code>a.b</code>
     */
    public static final PrecedenceLevel MemberAccess = Level1;
    /**
     * <code>a-&gt;b</code>
     */
    public static final PrecedenceLevel MemberAccessThroughPointer = Level1;
    /**
     * <code>a()</code>
     */
    public static final PrecedenceLevel FunctionCall = Level1;
    /**
     * <code>a[b]</code>
     */
    public static final PrecedenceLevel ArrayAccess = Level1;

    /**
     * <code>!a</code>
     */
    public static final PrecedenceLevel LogicalNot = Level2;
    /**
     * <code>+a</code>
     */
    public static final PrecedenceLevel UnaryPlus = Level2;
    /**
     * <code>-a</code>
     */
    public static final PrecedenceLevel UnaryMinus = Level2;
    /**
     * <code>(type)a</code>
     */
    public static final PrecedenceLevel Cast = Level2;
    /**
     * <code>++a</code>
     */
    public static final PrecedenceLevel PrefixIncrement = Level2;
    /**
     * <code>--a</code>
     */
    public static final PrecedenceLevel PrefixDecrement = Level2;
    /**
     * <code>*a</code>
     */
    public static final PrecedenceLevel Indirection = Level2;
    /**
     * <code>&amp;a</code>
     */
    public static final PrecedenceLevel AddressOf = Level2;

    /**
     * <code>a * b</code>
     */
    public static final PrecedenceLevel Multiplication = Level3;
    /**
     * <code>a / b</code>
     */
    public static final PrecedenceLevel Division = Level3;
    /**
     * <code>a % b</code>
     */
    public static final PrecedenceLevel Modulo = Level3;

    /**
     * <code>a + b</code>
     */
    public static final PrecedenceLevel Addition = Level4;
    /**
     * <code>a - b</code>
     */
    public static final PrecedenceLevel Subtraction = Level4;

    /**
     * <code>a &lt;&lt; b</code><br/>
     * <code>a &gt;&gt; b</code>
     */
    public static final PrecedenceLevel BitShift = Level5;

    /**
     * <code>a &gt; b</code>
     */
    public static final PrecedenceLevel GreaterThan = Level6;
    /**
     * <code>a &lt; b</code>
     */
    public static final PrecedenceLevel LessThan = Level6;

    /**
     * <code>a &gt;= b</code>
     */
    public static final PrecedenceLevel GreaterOrEqualTo = Level6;
    /**
     * <code>a &lt;= b</code>
     */
    public static final PrecedenceLevel LessOrEqualTo = Level6;

    /**
     * <code>a == b</code>
     */
    public static final PrecedenceLevel Equality = Level7;
    /**
     * <code>a != b</code>
     */
    public static final PrecedenceLevel NotEqual = Level7;

    /**
     * <code>a &amp; b</code>
     */
    public static final PrecedenceLevel BitwiseAnd = Level8;

    /**
     * <code>a ^ b</code>
     */
    public static final PrecedenceLevel BitwiseXor = Level9;

    /**
     * <code>a | b</code>
     */
    public static final PrecedenceLevel BitwiseOr = Level10;

    /**
     * <code>a &amp;&amp; b</code>
     */
    public static final PrecedenceLevel LogicalAnd = Level11;

    /**
     * <code>a || b</code>
     */
    public static final PrecedenceLevel LogicalOr = Level12;

    /**
     * <code>a ? ... : b</code>
     */
    public static final PrecedenceLevel TernaryConditional = Level13;

    /**
     * <code>a = b</code>
     */
    public static final PrecedenceLevel Assignment = Level14;

    /**
     * <code>a, b</code>
     */
    public static final PrecedenceLevel Comma = Level15;

    boolean isRightToLeft;

    private PrecedenceLevel(boolean isRightToLeft) {
	this.isRightToLeft = isRightToLeft;
    }

    public static boolean requireLeftParenthesis(PrecedenceLevel rootLevel, PrecedenceLevel leftLevel) {
	Preconditions.checkArgument(rootLevel != null);
	Preconditions.checkArgument(leftLevel != null);

	if (leftLevel == Unspecified || rootLevel == Unspecified) {
	    return true;
	}

	int rootOrdinal = rootLevel.ordinal();
	int leftOrdinal = leftLevel.ordinal();

	if (rootOrdinal < leftOrdinal) {
	    return true;
	}
	return rootOrdinal == leftOrdinal && rootLevel.isRightToLeft;
    }

    public static boolean requireRightParenthesis(PrecedenceLevel rootLevel, PrecedenceLevel rightLevel) {
	Preconditions.checkArgument(rootLevel != null);
	Preconditions.checkArgument(rightLevel != null);

	if (rightLevel == Unspecified || rootLevel == Unspecified) {
	    return true;
	}

	int rootOrdinal = rootLevel.ordinal();
	int rightOrdinal = rightLevel.ordinal();

	if (rootOrdinal < rightOrdinal) {
	    return true;
	}
	return rootOrdinal == rightOrdinal && !rootLevel.isRightToLeft;
    }

    public static boolean requireContentParenthesis(PrecedenceLevel rootLevel, PrecedenceLevel contentLevel) {
	Preconditions.checkArgument(rootLevel != null);
	Preconditions.checkArgument(contentLevel != null);

	if (contentLevel == Unspecified || rootLevel == Unspecified) {
	    return true;
	}

	int rootOrdinal = rootLevel.ordinal();
	int contentOrdinal = contentLevel.ordinal();

	return rootOrdinal < contentOrdinal;
    }
}
