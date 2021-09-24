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

package org.specs.MatlabAspects;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIR.Utilities.TypeDecoderUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;
import tir2tir.parser.AspectsParser;
import tir2tir.parser.AspectsParserTreeConstants;
import tir2tir.parser.ParseException;
import tir2tir.parser.SimpleNode;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabAspects {

    public final static String SCOPE_FUNCTION = "function";
    private final static String DEFAULT_REAL_STRING = "default_real";

    private ScopedMap<Symbol> table;
    private Map<String, Symbol> definitions;
    private ArrayList<Quantizer> quantizers;

    // private static FunctionSettings settings = FunctionSettings.getDefault();
    // private static MatrixImplementation impl = null;
    private static DataStore setup = null;
    private static Optional<Symbol> defaultReal = Optional.empty();

    /**
     * @param settings
     *            the settings to set
     */
    synchronized public static void setSettings(DataStore setup) {
        MatlabAspects.setup = setup;
    }

    public ArrayList<Quantizer> getQuantizers() {
        return quantizers;
    }

    public void setQuantizers(ArrayList<Quantizer> quantizers) {
        this.quantizers = quantizers;
    }

    private static boolean parsedCalled = false;

    /**
     * 
     */
    public MatlabAspects() {
        reset();
    }

    private void reset() {
        quantizers = new ArrayList<>();
        table = new ScopedMap<>();
        definitions = SpecsFactory.newHashMap();
    }

    /**
     * @param aspectsFileContents
     * @return
     */
    public static ScopedMap<Symbol> getSymbolMap(String aspectsFileContents) {

        SimpleNode aspectsParserTree = null;
        MatlabAspects.defaultReal = Optional.empty();

        try (InputStream inputStream = SpecsIo.toInputStream(aspectsFileContents)) {
            if (!MatlabAspects.parsedCalled) {
                MatlabAspects.parsedCalled = true;
                new AspectsParser(inputStream);
            } else {
                AspectsParser.ReInit(inputStream);
            }

            aspectsParserTree = AspectsParser.Start();

        } catch (Exception e) {
            e.printStackTrace();
            SpecsLogs.msgInfo(e.getMessage());
            return null;
        } catch (Error err) {
            SpecsLogs.msgInfo(err.getMessage());
            return null;
        }

        ScopedMap<Symbol> aspectsSymbolMap = generateSymbolMap(aspectsParserTree);
        return aspectsSymbolMap;
    }

    public static VariableType getTypeFromString(String str, DataStore setup) {
        return getTypeFromString(str, new MatlabAspectsUtils(setup).getDecoder());
    }

    public static VariableType getTypeFromString(String str, TypeDecoder decoder) {
        SimpleNode aspectsParserTree;

        try (InputStream inputStream = SpecsIo.toInputStream(str)) {
            if (!MatlabAspects.parsedCalled) {
                MatlabAspects.parsedCalled = true;
                new AspectsParser(inputStream);
            } else {
                AspectsParser.ReInit(inputStream);
            }

            aspectsParserTree = AspectsParser.DataType();

        } catch (Exception e) {
            SpecsLogs.msgInfo(e.getMessage());
            return null;
        } catch (Error err) {
            SpecsLogs.msgInfo(err.getMessage());
            return null;
        }

        Symbol symbol = new Symbol("type");
        new MatlabAspects().generateSymbolDataType(symbol, aspectsParserTree);
        return symbol.convert(decoder);
    }

    /**
     * @param startNode
     * @return
     */
    private static ScopedMap<Symbol> generateSymbolMap(SimpleNode startNode) {
        // Create AST parser
        MatlabAspects matlabASTParser = new MatlabAspects();

        // Process each node of the tree
        matlabASTParser.processNodeChildren(startNode, new ArrayList<String>());

        return matlabASTParser.table;
    }

    /**
     * @param child
     * @param arrayList
     */
    private void processNode(SimpleNode node, List<String> scope) {

        // Add Definitions
        if (node.getId() == AspectsParserTreeConstants.JJTDEFINITIONS) {
            processNodeChildren(node, scope);
            return;
        }

        // Add Define
        if (node.getId() == AspectsParserTreeConstants.JJTDEFINE) {
            addDefinition(node, scope);
            return;
        }

        // Add Scope
        if (node.getId() == AspectsParserTreeConstants.JJTSCOPE) {
            addScope(node, scope);
            return;
        }

        SpecsLogs.warn("Node type not implemented yet: '" + AspectsParserTreeConstants.jjtNodeName[node.getId()]
                + "'.");
    }

    /**
     * @param node
     * @param scope
     */
    private void addScope(SimpleNode scopeNode, List<String> scope) {

        String scopeName = (String) scopeNode.jjtGetValue();

        // Update current scope
        List<String> currentScope = new ArrayList<>(scope);
        currentScope.add(scopeName);

        // Check if it contains an aspect list
        if (scopeNode.jjtGetNumChildren() == 0) {
            return;
        }

        SimpleNode aspectList = (SimpleNode) scopeNode.jjtGetChild(0);

        // Process all children
        for (int a = 0; a < aspectList.jjtGetNumChildren(); a++) {
            SimpleNode child = (SimpleNode) aspectList.jjtGetChild(a);

            if (child.getId() == AspectsParserTreeConstants.JJTDATATYPEASPECT) {
                processAspect(child, currentScope);
            } else if (child.getId() == AspectsParserTreeConstants.JJTFUNCTIONTYPEASPECT) {
                processFunctionAspect(child, currentScope);
            } else if (child.getId() == AspectsParserTreeConstants.JJTSCOPE) {
                // addScope(child, currentScope);
                processNode(child, SpecsFactory.newArrayList(currentScope));
            } else {
                SpecsLogs.warn("Node type not implemented yet: '"
                        + AspectsParserTreeConstants.jjtNodeName[child.getId()] + "'.");
            }
        }

    }

    /**
     * @param aspect
     * @param currentScope
     */
    private void processAspect(SimpleNode aspect, List<String> currentScope) {

        Symbol newSymbol = null;
        for (int i = 0; i < aspect.jjtGetNumChildren(); i++) {
            SimpleNode aspectChild = (SimpleNode) aspect.jjtGetChild(i);

            if (aspectChild.getId() == AspectsParserTreeConstants.JJTVARIABLE) {
                newSymbol = new Symbol((String) aspectChild.jjtGetValue());
            } else if (aspectChild.getId() == AspectsParserTreeConstants.JJTDEFINEDBY) {
                String definer = (String) aspectChild.jjtGetValue();

                Symbol def = definitions.get(definer);
                if (def.definer == true) {
                    newSymbol.copySymbol(def);
                } else {
                    System.out.println("Error: Cannot use variable as a define: variable " + def.name
                            + " was used as a define");
                }
            } else if (aspectChild.getId() == AspectsParserTreeConstants.JJTDATATYPE) {
                // Create new symbol
                newSymbol = generateSymbolDataType(newSymbol, aspectChild);

                // Add it to the table
                table.addSymbol(currentScope, newSymbol.name, newSymbol);
            }

        }
    }

    /**
     * @param aspect
     * @param currentScope
     */
    private void processFunctionAspect(SimpleNode aspect, List<String> currentScope) {

        // System.out.println("Function Node, " + aspect.jjtGetNumChildren() + " children");

        // Function Name
        SimpleNode functionNameVariable = (SimpleNode) aspect.jjtGetChild(0);
        // Function Inputs
        SimpleNode dataTypeList = (SimpleNode) aspect.jjtGetChild(1);
        // Function Output
        SimpleNode returnDataType = (SimpleNode) aspect.jjtGetChild(2);

        // Create Symbol
        Symbol newSymbol = new Symbol((String) functionNameVariable.jjtGetValue());

        // Add return type
        newSymbol = generateSymbolDataType(newSymbol, returnDataType);

        // Get types scope
        List<String> inputTypes = getInputTypesString(dataTypeList);

        // If null, stop
        if (inputTypes == null) {
            return;
        }

        List<String> finalScope = SpecsFactory.newArrayList();
        finalScope.add(MatlabAspects.SCOPE_FUNCTION);
        finalScope.addAll(inputTypes);

        table.addSymbol(finalScope, newSymbol.name, newSymbol);

    }

    /**
     * @param dataTypeList
     * @return
     */
    private List<String> getInputTypesString(SimpleNode dataTypeList) {
        if (dataTypeList.getId() != AspectsParserTreeConstants.JJTDATATYPELIST) {
            throw new RuntimeException("Should be a DataTypeList");
        }

        List<String> dataTypeString = SpecsFactory.newArrayList();

        MatlabAspectsUtils matlabAspectsUtils = new MatlabAspectsUtils(MatlabAspects.setup);

        for (int i = 0; i < dataTypeList.jjtGetNumChildren(); i++) {
            SimpleNode aspectChild = (SimpleNode) dataTypeList.jjtGetChild(i);

            if (aspectChild.getId() != AspectsParserTreeConstants.JJTDATATYPE) {
                throw new RuntimeException("Should be a DataType");
            }

            // Create new symbol
            Symbol tempSymbol = new Symbol("temp_symbol");
            tempSymbol = generateSymbolDataType(tempSymbol, aspectChild);

            // VariableType vType = matlabAspectsUtils.convertSymbol(tempSymbol);
            VariableType vType = tempSymbol.convert(matlabAspectsUtils.getDecoder());

            // If null, stop
            if (vType == null) {
                String message = TypeDecoderUtils.getSupportTypesMsg(matlabAspectsUtils.getDecoder());
                SpecsLogs.msgInfo(message);
                return null;
            }

            dataTypeString.add(vType.getSmallId());
        }

        return dataTypeString;
    }

    /**
     * @param node
     * @param scope
     * @param table
     */
    private void addDefinition(SimpleNode define, List<String> scope) {

        Symbol newSymbol = new Symbol((String) define.jjtGetValue());
        newSymbol.setDefine(true);
        SimpleNode dataType = (SimpleNode) define.jjtGetChild(0);
        if (dataType.getId() == AspectsParserTreeConstants.JJTDATATYPE) {
            newSymbol = generateSymbolDataType(newSymbol, dataType);
        }

        definitions.put(newSymbol.name, newSymbol);

        // If default_real, set in setup
        if (newSymbol.name.equals(MatlabAspects.DEFAULT_REAL_STRING)) {
            MatlabAspects.defaultReal = Optional.of(newSymbol);
        }

    }

    /**
     * @param node
     * @param scope
     */
    private void processNodeChildren(SimpleNode node, List<String> scope) {

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            processNode(child, new ArrayList<String>());
        }

    }

    /**
     * @deprecated
     * @param aspectsFile
     * @return
     */

    @Deprecated
    public SymbolTable getSymbolTable(File aspectsFile) {
        return getSymbolTable(SpecsIo.read(aspectsFile));
    }

    /**
     * @param aspectsFileContents
     * @return
     * @deprecated
     */

    @Deprecated
    public SymbolTable getSymbolTable(String aspectsFileContents) {
        reset();
        InputStream inputStream = SpecsIo.toInputStream(aspectsFileContents);

        // AspectsParser.
        // Aspect parser
        // AspectsParser aspectsParser = new AspectsParser(
        // );
        SimpleNode aspectsParserTree = null;
        try {
            if (!MatlabAspects.parsedCalled) {
                MatlabAspects.parsedCalled = true;
                new AspectsParser(inputStream);
            } else {
                AspectsParser.ReInit(inputStream);
            }

            aspectsParserTree = AspectsParser.Start();
        } catch (ParseException e) {
            System.out.println("Aspect Parsing Exception:" + e);
            return null;
        }
        SymbolTable aspectsSymbolTable = generateSymbolTable(aspectsParserTree);
        // System.out.println("SymbolTable:\n" + aspectsSymbolTable);
        return aspectsSymbolTable;
    }

    /**
     * 
     * @param start
     * @return
     */

    private SymbolTable generateSymbolTable(SimpleNode start) {
        SymbolTable table = new SymbolTable();
        int sc = 0;
        if (start.jjtGetNumChildren() == 0) {
            return null;
            // skip Start node
        }

        if (start.jjtGetNumChildren() > 1) {
            SimpleNode definitionsList = (SimpleNode) start.jjtGetChild(sc++);

            for (int a = 0; a < definitionsList.jjtGetNumChildren(); a++) {
                SimpleNode definer = (SimpleNode) definitionsList.jjtGetChild(a);
                if (definer.getId() == AspectsParserTreeConstants.JJTDEFINE) {
                    Symbol s = new Symbol((String) definer.jjtGetValue());
                    s.setDefine(true);
                    SimpleNode dataType = (SimpleNode) definer.jjtGetChild(0);
                    if (dataType.getId() == AspectsParserTreeConstants.JJTDATATYPE) {
                        s = generateSymbolDataType(s, dataType);
                    }
                    table.add(s);
                }
            }
        }

        SimpleNode scopeNode = (SimpleNode) start.jjtGetChild(sc);
        // String scopeName = (String) scopeNode.jjtGetValue();

        if (scopeNode.jjtGetNumChildren() == 0) {
            return null;
        }
        SimpleNode aspectList = (SimpleNode) scopeNode.jjtGetChild(0);
        // percorrer os asps todos
        for (int a = 0; a < aspectList.jjtGetNumChildren(); a++) {
            SimpleNode aspect = (SimpleNode) aspectList.jjtGetChild(a);
            if (aspect.getId() == AspectsParserTreeConstants.JJTDATATYPEASPECT) {
                Symbol s = null;
                for (int b = 0; b < aspect.jjtGetNumChildren(); b++) {
                    SimpleNode aspectChild = (SimpleNode) aspect.jjtGetChild(b);
                    if (aspectChild.getId() == AspectsParserTreeConstants.JJTVARIABLE) {
                        s = new Symbol((String) aspectChild.jjtGetValue());
                    } else if (aspectChild.getId() == AspectsParserTreeConstants.JJTDEFINEDBY) {
                        String definer = (String) aspectChild.jjtGetValue();
                        Symbol def = table.get(definer);
                        if (def.definer == true) {
                            s.copySymbol(def);
                        } else {
                            System.out.println("Error: Cannot use variable as a define: variable " + def.name
                                    + " was used as a define");
                        }
                    }
                    // else if(aspectChild.id ==
                    // AspectsParserTreeConstants.JJTLABELBOUNDARY)
                    // {
                    // if(aspectChild.jjtGetNumChildren() > 1)
                    // {
                    // String initLabel = ((SimpleNode)
                    // aspectChild.jjtGetChild(0)).jjtGetValue().toString();
                    // String endLabel = ((SimpleNode)
                    // aspectChild.jjtGetChild(1)).jjtGetValue().toString();;
                    // }
                    // }
                    else if (aspectChild.getId() == AspectsParserTreeConstants.JJTDATATYPE) {
                        s = generateSymbolDataType(s, aspectChild);
                    }
                }
                if (s != null) {
                    table.add(s);
                }
            } else {
                System.err.print("Warning: sub-functions is not yet supported. ");
                System.err.println("Types for function '" + aspect.jjtGetValue() + "' will be ignored");
            }
        }

        return table;
    }

    // gera o tipo de variavel/define, conforme as propriedades na arvore
    private Symbol generateSymbolDataType(Symbol s, SimpleNode aspectChild) {

        s.matlabType = (String) aspectChild.jjtGetValue();
        if (s.matlabType.equals("float")) {
            s.matlabType = "single";
        }

        // check fixed type
        if (s.matlabType.equals("fixed") || s.matlabType.equals("ufixed")) {
            for (int c = 0; c < aspectChild.jjtGetNumChildren(); c++) {
                SimpleNode quantizerNode = (SimpleNode) aspectChild.jjtGetChild(c);
                if (quantizerNode.getId() == AspectsParserTreeConstants.JJTDATATYPEQUANTIZER) {
                    Quantizer qTmp = new Quantizer(quantizers.size());
                    // Sinal
                    qTmp.add("mode", s.matlabType);
                    if (s.matlabType.equals("fixed")) {
                        qTmp.S = 1;
                    } else {
                        qTmp.S = 0;
                    }
                    // ler inteiro WL
                    int WL = Integer.parseInt(((String) ((SimpleNode) quantizerNode.jjtGetChild(0)).jjtGetValue()));
                    int FL = Integer.parseInt(((String) ((SimpleNode) quantizerNode.jjtGetChild(1)).jjtGetValue()));
                    qTmp.set(WL, FL);

                    for (int d = 0; d < quantizerNode.jjtGetNumChildren(); d++) {
                        SimpleNode propsListNode = ((SimpleNode) quantizerNode.jjtGetChild(d));
                        if (propsListNode.getId() == AspectsParserTreeConstants.JJTPROPERTIESLIST) {

                            for (int e = 0; e < propsListNode.jjtGetNumChildren(); e++) {
                                SimpleNode propsNode = ((SimpleNode) propsListNode.jjtGetChild(e));
                                if (propsNode.getId() == AspectsParserTreeConstants.JJTPROPERTIESSET) {
                                    String prop = (String) propsNode.jjtGetValue();
                                    String value = (String) ((SimpleNode) propsNode.jjtGetChild(0)).jjtGetValue();
                                    qTmp.add(prop, value);

                                }
                            }

                        }
                    }
                    int indexOfQuantizer = quantizers.indexOf(qTmp);
                    if (indexOfQuantizer < 0 || indexOfQuantizer >= quantizers.size()) {
                        quantizers.add(qTmp);
                        s.quantizerObject = qTmp.index;
                        // System.out.println(qTmp);
                    } else {
                        s.quantizerObject = indexOfQuantizer;
                    }
                }
            }
        }

        // Set the dimension
        setDimensionType(s, aspectChild);

        return s;
    }

    /**
     * @param s
     * @param aspectChild
     */
    private static void setDimensionType(Symbol s, SimpleNode aspectChild) {
        // Check dimension types
        if (aspectChild.jjtGetNumChildren() == 0) {
            s.dimType = Symbol.NONE;
        }

        // Go over children
        for (int c = 0; c < aspectChild.jjtGetNumChildren(); c++) {
            SimpleNode dataDim = (SimpleNode) aspectChild.jjtGetChild(c);
            if (dataDim.getId() == AspectsParserTreeConstants.JJTDATATYPEMULTI) {
                s.dimType = Symbol.MULTI;
                continue;
            }

            /*
            if (dataDim.getId() == AspectsParserTreeConstants.JJTDATATYPEDIMXDIM) {
            s.dimType = Symbol.HXW;
            String dimxdim = (String) dataDim.jjtGetValue();
            int pos = dimxdim.indexOf('x');
            
            String hStr, wStr;
            hStr = dimxdim.substring(0, pos);
            wStr = dimxdim.substring(pos + 1);
            
            if (hStr.equals("*")) {
                s.dimH = Symbol.DIM_STAR;
            } else {
                s.dimH = Integer.parseInt(hStr);
            }
            
            if (wStr.equals("*")) {
                s.dimW = Symbol.DIM_STAR;
            } else {
                s.dimW = Integer.parseInt(wStr);
            }
            
            continue;
            }
            */

            // if (dataDim.getId() == AspectsParserTreeConstants.JJTARRAYUNDEFINED) {
            // s.dimType = Symbol.UNDEFINED_ARRAY;
            // }

            /*
             * 
            if (dataDim.getId() == AspectsParserTreeConstants.JJTUNBOUNDEDARRAYDIM) {
            s.dimType = Symbol.UNBOUNDED_ARRAY;
            String dim = (String) dataDim.jjtGetValue();
            int starIndex = dim.indexOf('*');
            String dimInt = dim.substring(0, starIndex);
            // Parse dimension number
            s.numDim = Integer.parseInt(dimInt);
            continue;
            }
            */

            // if (dataDim.getId() == AspectsParserTreeConstants.JJTARRAYSHAPE) {
            if (dataDim.getId() == AspectsParserTreeConstants.JJTSHAPE) {
                s.dimType = Symbol.ARRAY_SHAPE;
                parseArrayShape(s, (String) dataDim.jjtGetValue());
                continue;
            }

            // if (dataDim.getId() == AspectsParserTreeConstants.JJTARRAYUNDEFINED) {
            if (dataDim.getId() == AspectsParserTreeConstants.JJTUNDEFINEDSHAPE) {
                s.dimType = Symbol.UNDEFINED_ARRAY;

                // List<String> result = ParseUtils.getRegex((String) dataDim.jjtGetValue(), "((\\[\\])+)");
                // s.setMatrixShape(MatrixShape.newDimsShape(result.get(0).length() / 2));

                // Determine number of dimensions, and set shape
                // Number of dimensions is the size of the captured string divided by two
                // String undefinedArrayShape = (String) dataDim.jjtGetValue();
                // s.setMatrixShape(TypeShape.newDimsShape(undefinedArrayShape.length() / 2));
                s.setMatrixShape(TypeShape.newUndefinedShape());
                continue;
            }
            if (dataDim.getId() == AspectsParserTreeConstants.JJTUNDEFINEDCELLSHAPE) {
                s.setCellShape(TypeShape.newUndefinedShape());
                continue;
            }

            // LoggingUtils.msgWarn("Case not defined:"+AspectsParserTreeConstants.jjtNodeName[dataDim.getId()]);
        }

    }

    /**
     * @param s
     * @param arrayShape
     */
    private static void parseArrayShape(Symbol s, String arrayShape) {

        List<Integer> dims = new ArrayList<>();
        int index = 0;
        while (index < arrayShape.length()) {
            assert arrayShape.charAt(index) == '[';
            int endIndex = arrayShape.indexOf(']', index);

            // Found empty shape
            if (endIndex - index == 1) {
                dims.add(-1);
                index = endIndex + 1;
                continue;
            }

            // Found defined shape. Using decode because it returns an Integer
            Integer decodedInt = Integer.decode((arrayShape.substring(index + 1, endIndex)));
            dims.add(decodedInt);

            index = endIndex + 1;
        }

        /*
        	String noRightBracket = arrayShape.replaceAll("]", "");
        	String noFirstLeftBracket = noRightBracket.substring(1);
        	String[] dims = noFirstLeftBracket.split(Pattern.quote("["));
        	List<Integer> parsedDims = FactoryUtils.newArrayList();
        	for (String dim : dims) {
        	    int intDim = ParseUtils.parseInt(dim);
        	    // s.addDimToShape(intDim);
        	    parsedDims.add(intDim);
        	}
        */
        s.setMatrixShape(TypeShape.newInstance(dims));

    }

    public static Optional<Symbol> getDefaultReal() {
        return MatlabAspects.defaultReal;
    }
}
