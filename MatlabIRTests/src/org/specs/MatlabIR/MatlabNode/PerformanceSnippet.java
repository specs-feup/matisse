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

package org.specs.MatlabIR.MatlabNode;


public class PerformanceSnippet {

    /**
     * On Core2Quad: <br>
     * ENUM TIME:68,3ms - Statements: 1.000.000<br>
     * CLASS TIME:51,32ms - Statements: 1.000.000
     */
    /*  
      @Test
      public void typeVsClass() {

    int numStatements = 1000000;
    List<MatlabNode> statements = IntStream.range(0, numStatements)
    	.mapToObj(
    		i -> StatementFactory.newFunctionDeclaration(i, "dummyFunction", Arrays.asList("input1"),
    			Arrays.asList("output1")))
    	.collect(Collectors.toList());

    long tic = System.nanoTime();
    long numberOfFuncDec = statements.stream()
    	.filter(node -> StatementUtils.is(node, MStatementType.FunctionDeclaration))
    	.count();
    long toc = System.nanoTime();
    System.out.println("ENUM TIME:" + ParseUtils.parseTime(toc - tic) + " - Statements: " + numberOfFuncDec);

    tic = System.nanoTime();
    numberOfFuncDec = statements.stream()
    	.filter(node -> StatementUtils.is(node, FunctionDeclarationSt.class))
    	.count();
    toc = System.nanoTime();
    System.out.println("CLASS TIME:" + ParseUtils.parseTime(toc - tic) + " - Statements: " + numberOfFuncDec);
      }
    */
}
