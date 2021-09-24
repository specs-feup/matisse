/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIR.Language.Types;

import java.util.EnumSet;

/**
 * @author Joao Bispo
 * 
 */
public enum CDataType {

    DOUBLE("double"),
    FLOAT("float"),
    INT("int"),
    CHAR("char");

    // private final int priority;
    private final String declarationName;

    private static final EnumSet<CDataType> integerTypes = EnumSet.of(INT, CHAR);

    /*
    private static final EnumSet<CTypev2> floatingTypes = EnumSet.of(Float, Double);
    */

    private CDataType(String declarationName) {
	// this.priority = CTypeV2.values().length - ordinal();
	this.declarationName = declarationName;
    }

    /**
     * 
     * @param targetType
     * @return true if the current type fits into the given type
     */
    /*
    public boolean fitsInto(CDataType targetType) {
	return this.ordinal() >= targetType.ordinal();
    }
    */
    
    /**
     * @return
     */
    /*
    public String getSmallId() {
    switch (this) {
    case Double:
        return "d";
    case Float:
        return "f";
    case Int8:
        return "i8";
    case Int16:
        return "i16";
    case Int32:
        return "i32";
    case Int64:
        return "i64";
    case Cint:
        // return "int";
        return "i";
    case Char:
        return "c";
    case Uint8:
        return "ui8";
    case Uint16:
        return "ui16";
    case Uint32:
        return "ui32";
    case Uint64:
        return "ui64";
    default:
        LoggingUtils.msgWarn("Case not defined:" + this);
        return null;
    }
    }
    */
    /*
    private final static Map<String, CTypev2> nameMap = EnumUtils.buildMap(CTypev2.class);

    private final static Map<CTypev2, String> printfSymbol;
    static {
    printfSymbol = new EnumMap<CTypev2, String>(CTypev2.class);

    printfSymbol.put(Double, "%e");
    printfSymbol.put(Float, "%e");
    printfSymbol.put(Int64, "%\" PRId64 \"");
    printfSymbol.put(Uint64, "%\" PRIu64 \"");
    // printfSymbol.put(Int32, "%d");

    // printfSymbol.put(Cint, "%d");
    }
    */

    /**
     * @param string
     * @return
     */
    /*
    public static CTypev2 getNumericType(String string) {
    return nameMap.get(string);
    }
    */
    /*
        public String getPrintfSymbol() {
    	String s = printfSymbol.get(this);
    	if (s == null) {
    	    // LoggingUtils.msgWarn("Printf symbol for '"+this+"' not defined. Using '%d' as default.");
    	    s = "%d";
    	}
    	return s;
        }
    */
    /**
     * The C code used to declare this type.
     * 
     * @return
     */
    /*
    public String getDeclarationCode() {

    // Check if stdin type
    String declaration = CirUtils.getStdintDeclaration(this);
    if (declaration != null) {
        return declaration;
    }

    // Is not stdint, use specific C name
    switch (this) {
    case Cint:
        return "int";
    case Double:
        return "double";
    case Float:
        return "float";
    case Char:
        return "char";
    default:
        LoggingUtils.msgWarn("Case not defined:" + this);
        return null;
    }
    }
    */
    /**
     * @param nextNumeric
     * @return true if the current type has higher or same priority as the given type. False otherwise
     */
    /*
    public boolean isHigherOrSamePriority(CTypev2 nextNumeric) {
    if (this.priority >= nextNumeric.priority) {
        return true;
    } else {
        return false;
    }
    }
    */

    /**
     * 
     * 
     * @param numericTypes
     * @return the NumericType in the list with higher priority
     */
    /*
    public static CTypev2 getHighestPriority(List<CTypev2> numericTypes) {

    if (numericTypes.isEmpty()) {
        return null;
    }

    CTypev2 higherPriority = numericTypes.get(0);

    for (int i = 1; i < numericTypes.size(); i++) {
        CTypev2 otherArg = numericTypes.get(i);

        // Compare with args
        if (!higherPriority.isHigherOrSamePriority(otherArg)) {
    	higherPriority = otherArg;
        }
    }

    return higherPriority;
    }
    */
    /**
     * Returns the numeric type with the lowest priority.
     * 
     * @param numericTypes
     * @return
     */
    /*
    public static CTypev2 getLowestPriority(List<CTypev2> numericTypes) {

    if (numericTypes.isEmpty()) {
        return null;
    }

    CTypev2 higherPriority = numericTypes.get(0);

    for (int i = 1; i < numericTypes.size(); i++) {
        CTypev2 otherArg = numericTypes.get(i);

        // Compare with args
        if (higherPriority.isHigherOrSamePriority(otherArg)) {
    	higherPriority = otherArg;
        }
    }

    return higherPriority;
    }
    */

    public boolean isInteger() {
	return integerTypes.contains(this);
    }

    /*
        public boolean isFloating() {
        return floatingTypes.contains(this);
        }
        */
    /**
     * Returns the NumericType with highest priority.
     * 
     * <p>
     * Currently, this type is 'double'.
     * 
     * @return
     */
    /*
    public static CTypev2 getHighestPriority() {
    return Double;
    }
    */

    /**
     * Returns the includes needed to use this NumericType.
     * 
     * @return
     */
    /*
    public Set<String> getIncludes() {
    Set<String> includes = new HashSet<String>();

    // Check if numeric type is inside 'stdint'
    if (CirUtils.isStdint(this)) {
        includes.add(SystemInclude.Stdint.getIncludeName());
    }

    if (this == Int64 || this == Uint64) {
        includes.add(SystemInclude.IntTypes.getIncludeName());
    }

    return includes;
    }
    */
    /*
    public boolean isUnsigned() {
    return isUnsigned;
    }
    */

    /**
     * @return a list with all the integer numeric types
     */
    /*
    public static List<CTypev2> getAllIntegers() {

    return Arrays.asList(Cint, Int8, Int16, Int32, Int64, Uint8, Uint16, Uint32, Uint64);
    }
    */

    public String getDeclaration() {
	return declarationName;
    }
}
