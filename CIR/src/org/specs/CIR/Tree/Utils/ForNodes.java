package org.specs.CIR.Tree.Utils;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.ReservedWord;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.AssignmentNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.InstructionNode;
import org.specs.CIR.Tree.CNodes.ReservedWordNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Utilities.CirBuilder;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * TODO: Do not extend AInstanceBuilder, this is for builder CNodes.
 * 
 * @author JoaoBispo
 *
 */
public class ForNodes extends CirBuilder {

    private final CNodeFactory cnodes;

    /**
     * Helper constructor that accepts a ProviderData
     * 
     * @param data
     */
    public ForNodes(ProviderData data) {
        this(data.getSettings());
    }

    public ForNodes(DataStore setup) {
        super(setup);
        cnodes = new CNodeFactory(setup);
    }

    /**
     * Builds a CToken representing a for loop.
     * 
     * @param inductionVar
     * @param startValue
     * @param endValue
     * @param op
     * @param increment
     *            can be null
     * @return
     */
    public CNode newForInstruction(Variable inductionVar, CNode startValue, COperator stopOp, CNode endValue,
            COperator incrementOp, CNode increment) {

        // Build assignment between the induction variable and the start value
        // CToken assignment = CTokenFactory.newAssignment(inductionVar, CTokenFactory.newExpression(startValue));
        AssignmentNode assignment = CNodeFactory.newAssignment(inductionVar, startValue);

        FunctionCallNode stopFunc = cnodes.newFunctionCall(stopOp, CNodeFactory.newVariable(inductionVar), endValue);

        // Build expression just with 'stop' function call
        // CToken stopExpr = CTokenFactory.newExpression(stopFunc);
        FunctionCallNode stopExpr = stopFunc;

        // CToken incrExpr = buildIncrementExpression(increment);
        CNode incrExpr = buildIncrementExpression(inductionVar, increment, incrementOp);

        return newForInstruction(assignment, stopExpr, incrExpr);
    }

    /**
     * Builds a CToken representing a for loop.
     * 
     * @param assignment
     *            The loop assignment statement. Must not be null.
     * @param stopExpr
     *            The loop condition. Must not be null.
     * @param incrExpr
     *            The loop final expression, which executes every time an iteration finishes. Must not be null.
     * @return
     */
    public InstructionNode newForInstruction(AssignmentNode assignment, CNode stopExpr, CNode incrExpr) {
        // TODO: Perhaps null should be a valid value? (for (;;) {})

        checkArgument(assignment != null);
        checkArgument(stopExpr != null);
        checkArgument(incrExpr != null);

        CNode reservedWordToken = CNodeFactory.newReservedWord(ReservedWord.For);

        return CNodeFactory.newInstruction(InstructionType.For, reservedWordToken, assignment, stopExpr, incrExpr);
    }

    /**
     * Builds the expression for the increment.
     * 
     * @param inductionVar
     * @param increment
     * @param incrementOp
     * @return
     */
    private CNode buildIncrementExpression(Variable inductionVar, CNode increment, COperator incrementOp) {

        if (increment == null) {
            CNode inducVar = CNodeFactory.newVariable(inductionVar);

            // return incrementOp.getFunctionCall(inducVar);
            return cnodes.newFunctionCall(incrementOp, inducVar);
        }

        CNode rightHandInducVar = CNodeFactory.newVariable(inductionVar);

        // CToken incrementFunc = incrementOp.getFunctionCall(rightHandInducVar, increment);
        CNode incrementFunc = cnodes.newFunctionCall(incrementOp, rightHandInducVar, increment);

        return CNodeFactory.newAssignment(inductionVar, incrementFunc);
    }

    /**
     * Builds a new FOR loop block.
     * 
     * @param inductionVar
     *            - the induction variable
     * @param startValue
     *            - the start value of the loop
     * @param stopOp
     *            - the operator used to stop the execution
     * @param endValue
     *            - the end value of the loop
     * @param incrementOp
     *            - the operator used to increment the induction variable
     * @param increment
     *            - the increment to the induction variable ( can be null, defaults to 1 )
     * @param bodyInstructions
     *            - the instructions inside the loop body
     * @return a {@link CNode} with a block containing the loop and its instructions
     */
    public CNode newForLoopBlock(Variable inductionVar, CNode startValue, COperator stopOp, CNode endValue,
            // COperator incrementOp, CToken increment, List<CToken> bodyInstructions) {
            COperator incrementOp, CNode increment, CInstructionList bodyInstructions) {

        // The list with the block instructions
        // List<CToken> blockInstructions = FactoryUtils.newArrayList();

        // Build an assignment between the induction variable and the start value
        // CToken assignment = CTokenFactory.newAssignment(inductionVar, CTokenFactory.newExpression(startValue));
        AssignmentNode assignment = CNodeFactory.newAssignment(inductionVar, startValue);

        // Build an expression just with the function call to the stop operator
        // CToken stopFunc = stopOp.getFunctionCall(CTokenFactory.newVariable(inductionVar), endValue);
        FunctionCallNode stopFunc = cnodes.newFunctionCall(stopOp, CNodeFactory.newVariable(inductionVar), endValue);
        // CToken stopExpr = CTokenFactory.newExpression(stopFunc);
        FunctionCallNode stopExpr = stopFunc;

        // The increment expression
        CNode incrExpr = buildIncrementExpression(inductionVar, increment, incrementOp);

        // Create the FOR instruction and save it
        ReservedWordNode reservedWordToken = CNodeFactory.newReservedWord(ReservedWord.For);
        InstructionNode forInstruction = CNodeFactory.newInstruction(InstructionType.For, reservedWordToken, assignment,
                stopExpr, incrExpr);

        // List<InstructionNode> blockInstructions = FactoryUtils.newArrayList();
        List<CNode> blockInstructions = SpecsFactory.newArrayList();
        blockInstructions.add(forInstruction);
        blockInstructions.addAll(bodyInstructions.get());
        /*
        // Process each body instruction and save it
        for (CToken bodyI : bodyInstructions) {
        
            if (bodyI.getType() == CTokenType.Block || bodyI.getType() == CTokenType.Instruction || bodyI.getType() == CTokenType.Comment) {
        	blockInstructions.add(bodyI);
            } else {
        	blockInstructions.add(CTokenFactory.newInstruction(InstructionType.Undefined, bodyI));
            }
        }
        */

        // Build a block with all the instructions and return
        CNode forLoopBlock = CNodeFactory.newBlock(blockInstructions);

        return forLoopBlock;

    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param inductionVar
     * @param startValue
     * @param stopOp
     * @param endValue
     * @param incrementOp
     * @param increment
     * @param bodyInstructions
     * @return
     */
    public CNode newForLoopBlock(Variable inductionVar, CNode startValue, COperator stopOp, CNode endValue,
            COperator incrementOp, CNode increment, CNode... bodyInstructions) {

        Preconditions.checkArgument(increment != null);

        return newForLoopBlock(inductionVar, startValue, stopOp, endValue, incrementOp, increment,
                Arrays.asList(bodyInstructions));
    }

    /**
     * Builds a new FOR loop block.
     * 
     * @param inductionVar
     *            - the induction variable
     * @param startValue
     *            - the start value of the loop
     * @param stopOp
     *            - the operator used to stop the execution
     * @param endValue
     *            - the end value of the loop
     * @param incrementOp
     *            - the operator used to increment the induction variable
     * @param increment
     *            - the increment to the induction variable ( can be null, defaults to 1 )
     * @param bodyInstructions
     *            - the instructions inside the loop body
     * @return a {@link CNode} with a block containing the loop and its instructions
     */
    public CNode newForLoopBlock(Variable inductionVar, CNode startValue, COperator stopOp, CNode endValue,
            COperator incrementOp, CNode increment, List<CNode> bodyInstructions) {

        CInstructionList bodyInstructionsList = new CInstructionList();
        for (CNode inst : bodyInstructions) {
            bodyInstructionsList.addInstruction(inst, null);
        }

        // List<CToken> bodyInstructionsList = Arrays.asList(bodyInstructions);

        return newForLoopBlock(inductionVar, startValue, stopOp, endValue, incrementOp, increment,
                bodyInstructionsList);
    }

    /**
     * Helper method with variadic inputs. Automatically sets
     * 
     * <p>
     * - Start value to 0;<br>
     * - stop operation to less;<br>
     * - increment operation to addition;<br>
     * - increment to 1;<br>
     * 
     * @param inductionVar
     * @param endValue
     * @param bodyInstructions
     * @return
     */
    public CNode newForLoopBlock(VariableNode inductionVar, CNode endValue, CNode... bodyInstructions) {

        return newForLoopBlock(inductionVar, endValue, Arrays.asList(bodyInstructions));
    }

    /**
     * Helper method with automatically sets:
     * 
     * <p>
     * - Start value to 0;<br>
     * - stop operation to less;<br>
     * - increment operation to addition;<br>
     * - increment to 1;<br>
     * 
     * @param inductionVar
     * @param endValue
     * @param bodyInstructions
     * @return
     */
    public CNode newForLoopBlock(VariableNode inductionVar, CNode endValue, List<CNode> bodyInstructions) {

        // CToken startValue = CToken.build(0);
        CNode startValue = CNodeFactory.newCNumber(0, getNumerics().newInt());
        COperator stopOp = COperator.LessThan;
        COperator incrementOp = COperator.Addition;
        // CToken increment = CToken.build(1);
        CNode increment = CNodeFactory.newCNumber(1, getNumerics().newInt());

        Variable induction = inductionVar.getVariable();

        return newForLoopBlock(induction, startValue, stopOp, endValue, incrementOp, increment, bodyInstructions);
    }

    /**
     * Helper method with automatically sets:
     * 
     * <p>
     * - increment operation to addition;<br>
     * - increment to 1;<br>
     * 
     * @param inductionVar
     * @param endValue
     * @param bodyInstructions
     * @return
     */
    public CNode newForLoopBlock(VariableNode inductionVar, CNode startValue, CNode endValue, COperator stopOp,
            List<CNode> bodyInstructions) {

        COperator incrementOp = COperator.Addition;
        // CToken increment = CToken.build(1);
        CNode increment = CNodeFactory.newCNumber(1, getNumerics().newInt());

        Variable induction = inductionVar.getVariable();

        return newForLoopBlock(induction, startValue, stopOp, endValue, incrementOp, increment, bodyInstructions);
    }
}
