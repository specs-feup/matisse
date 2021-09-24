package org.specs.MatlabToCTester.Outputs;

import java.util.List;

/**
 * Represents the output of a single test. A test is the execution of a binary with several test cases (inputs).
 * 
 * @author Pedro Pinto
 *
 */
public class TestOutput {

    private String testName;
    private List<TestCaseOutput> testCaseOutputs;
    
    
    /**
     * 
     * @param testName
     * 		- the name of the test (should be the name of the function)
     * @param testCaseOutputs
     * 		- the outputs of all the test cases for this test
     */
    public TestOutput(String testName, List<TestCaseOutput> testCaseOutputs) {
	this.testName = testName;
	this.testCaseOutputs = testCaseOutputs;
    }

    
    public String getTestName() {
        return testName;
    }

    public List<TestCaseOutput> getTestCaseOutputs() {
        return testCaseOutputs;
    }



    @Override
    public String toString() {
	
	StringBuilder builder = new StringBuilder();
	
	builder.append("----------------------------------------------\n");
	
	builder.append("Function \"" + testName + "\":\n");
	
	for (TestCaseOutput testCase : testCaseOutputs) {
	    builder.append(testCase.toString());
	}
	
	return builder.toString();
    }


    /**
     * Prints a list of TestOutput.
     * 
     * @param outputs
     * 		- the list
     */
    public static void printList(List<TestOutput> outputs) {

	for (TestOutput testOutput : outputs) {
	    System.out.println(testOutput);
	}
    }
    
    
    
}
