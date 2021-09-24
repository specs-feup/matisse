package org.specs.MatlabProcessor.MatlabParser.Rules;

import java.util.ArrayList;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.MatlabOperator;
import org.specs.MatlabIR.MatlabLanguage.ReservedWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatrixNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ParenthesisNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.ReservedWordNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.RowNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.AssignmentNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SpaceNode;
import org.specs.MatlabIR.MatlabNode.nodes.temporary.SubscriptSeparatorNode;
import org.specs.MatlabIR.matlabrules.MatlabNodeRule;
import org.specs.MatlabProcessor.Exceptions.ParserErrorException;
import org.specs.MatlabProcessor.Reporting.ProcessorErrorType;

import pt.up.fe.specs.util.reporting.Reporter;

public final class FunctionDeclarationBuilderRule implements MatlabNodeRule {

    private final Reporter reportService;

    public FunctionDeclarationBuilderRule(Reporter reportService) {
	this.reportService = reportService;
    }

    private static boolean check(MatlabNode token) {
	if (!(token instanceof StatementNode)) {
	    return false;
	}

	// List<MatlabNode> tokens = token.getChildren();

	// Check if it has children
	if (!token.hasChildren()) {
	    // if (tokens.isEmpty()) {
	    return false;
	}

	// Check if first token is a reserved word
	MatlabNode firstChild = token.getChild(0);
	if (!(firstChild instanceof ReservedWordNode)) {
	    // if (tokens.get(0).getType() != MType.ReservedWord) {
	    return false;
	}

	// Check if first token is the function keyword
	ReservedWord reservedWord = ((ReservedWordNode) firstChild).getWord();
	if (reservedWord != ReservedWord.Function) {
	    // String reservedWordString = MatlabTokenContent.getReservedWordString(tokens.get(0));
	    // if (!reservedWordString.equals(ReservedWord.Function.getLiteral())) {
	    return false;
	}

	return true;
    }

    private enum FunctionState {
	AfterFunctionKeyword,
	AfterOutputsList,
	AfterIdentifierBeforeAssignment,
	AfterAssignment,
	AfterInputs
    }

    @Override
    public MatlabNode apply(MatlabNode token) {

	if (!check(token)) {
	    return token;
	}

	// StatementData stData = CompatibilityUtils.getData(token);

	// Remove spaces
	token.removeChildren(SpaceNode.class);
	// TokenUtils.removeChildren(token, MType.Space);

	List<MatlabNode> tokens = token.getChildren();

	// assert tokens.get(0).getType() == MType.ReservedWord; // Function symbol

	FunctionState state = FunctionState.AfterFunctionKeyword;

	IdentifierNode candidateIdentifier = null;
	String functionName = null;
	List<MatlabNode> outputs = new ArrayList<>();
	List<MatlabNode> inputs = new ArrayList<>();

	for (int i = 1; i < tokens.size(); ++i) {
	    MatlabNode node = tokens.get(i);

	    switch (state) {
	    case AfterFunctionKeyword:
		if (node instanceof IdentifierNode) {
		    candidateIdentifier = (IdentifierNode) node;
		    state = FunctionState.AfterIdentifierBeforeAssignment;
		} else if (node instanceof MatrixNode) {
		    buildOutputs(outputs, (MatrixNode) node);
		    state = FunctionState.AfterOutputsList;
		} else if (node instanceof AccessCallNode) {
		    AccessCallNode accessCall = (AccessCallNode) node;
		    functionName = accessCall.getName();
		    buildInputs(inputs, accessCall.getArguments());
		    state = FunctionState.AfterInputs;
		} else {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Unexpected token " + node.getNodeName()
				    + " in function declaration");
		}
		break;
	    case AfterIdentifierBeforeAssignment:
		if (node instanceof AssignmentNode) {
		    outputs.add(candidateIdentifier);
		    state = FunctionState.AfterAssignment;
		} else {
		    throw new ParserErrorException("Unexpected token " + node.getNodeName()
			    + " in function declaration");
		}
		break;
	    case AfterOutputsList:
		if (node instanceof AssignmentNode) {
		    state = FunctionState.AfterAssignment;
		} else {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Unexpected token " + node.getNodeName()
				    + " after outputs list in function declaration");
		}
		break;
	    case AfterAssignment:
		if (node instanceof IdentifierNode) {
		    functionName = ((IdentifierNode) node).getName();
		    state = FunctionState.AfterInputs;
		} else if (node instanceof AccessCallNode) {
		    AccessCallNode accessCall = (AccessCallNode) node;
		    functionName = accessCall.getName();
		    buildInputs(inputs, accessCall.getArguments());
		    state = FunctionState.AfterInputs;
		} else if (node instanceof ReservedWordNode
			&& ((ReservedWordNode) node).getWord() == ReservedWord.End) {
		    // Surprisingly, end is a valid function name.
		    // However, only in function x = end().
		    // function end() does not work.
		    functionName = "end";

		    if (tokens.size() != i - 1) {
			MatlabNode potentialParenthesisNode = tokens.get(i + 1);
			if (potentialParenthesisNode instanceof ParenthesisNode) {
			    buildEndInputs(inputs, potentialParenthesisNode);
			    i++;
			}
		    }
		    state = FunctionState.AfterInputs;
		} else {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Unexpected token " + node.getNodeName()
				    + " after assignment symbol in function declaration.");
		}
		break;
	    case AfterInputs:
		throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			"Could not parse function declaration list: " + node + "\nAt " + token);
	    default:
		throw new ParserErrorException("TODO: " + state + ": " + node);
	    }
	}

	if (state == FunctionState.AfterAssignment) {
	    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Incomplete function declaration");
	} else if (state == FunctionState.AfterIdentifierBeforeAssignment) {
	    if (candidateIdentifier == null) {
		throw reportService.error("Candidate indentifier is null");
	    }
	    functionName = candidateIdentifier.getName();
	}
	// Token is a statement
	StatementNode statement = (StatementNode) token;
	return StatementFactory.newFunctionDeclaration(statement.getLine(), functionName, inputs, outputs);
    }

    private void buildOutputs(List<MatlabNode> outputs, MatrixNode matrix) {
	List<RowNode> rows = matrix.getRows();
	if (rows.size() > 1) {
	    throw reportService
		    .emitError(ProcessorErrorType.PARSE_ERROR, "Function outputs can't have multiple rows");
	}
	if (rows.size() == 1) {
	    buildOutputs(outputs, rows.get(0));
	}
    }

    private void buildOutputs(List<MatlabNode> outputs, RowNode row) {
	for (MatlabNode node : row.getChildren()) {
	    if (node instanceof IdentifierNode) {
		outputs.add(node);
	    } else if (node instanceof SpaceNode || node instanceof SubscriptSeparatorNode) {
		// Ignore
	    } else {
		throw reportService.emitError(ProcessorErrorType.PARSE_ERROR, "Unexpected token " + node
			+ " in function outputs list.");
	    }
	}
    }

    // FIXME: This function is also used by the tokenizer, due to the way lambda inputs are handled.
    // Perhaps it should be moved elsewhere so we can make FunctionDeclarationBuilderRule package-internal?
    public void buildInputs(List<MatlabNode> inputs, List<MatlabNode> arguments) {
	boolean needsSubscript = false;
	boolean afterComma = false;
	boolean argumentsStart = true;

	for (MatlabNode node : arguments) {
	    if (node instanceof IdentifierNode) {
		if (needsSubscript) {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Missing comma (,) before identifier in function inputs.");
		}
		needsSubscript = true;
		afterComma = false;
		argumentsStart = false;

		inputs.add(node);
	    } else if (node instanceof OperatorNode
		    && ((OperatorNode) node).getOp() == MatlabOperator.LogicalNegation) {
		if (needsSubscript) {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Missing comma (,) before ~ in function inputs.");
		}
		needsSubscript = true;
		afterComma = false;
		argumentsStart = false;

		inputs.add(MatlabNodeFactory.newUnusedVariable());
	    } else if (node instanceof SubscriptSeparatorNode) {
		if (afterComma) {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Duplicate comma (,) in function inputs.");
		}
		if (argumentsStart) {
		    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			    "Extraneous leading comma (,) in function inputs");
		}

		needsSubscript = false;
		afterComma = true;
	    } else if (node instanceof SpaceNode) {
		// Ignore
	    } else {
		throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
			"Unexpected token " + node.getNodeName() + " in function inputs");
	    }
	}

	if (afterComma) {
	    throw reportService.emitError(ProcessorErrorType.PARSE_ERROR,
		    "Extraneous trailing comma (,) in function inputs.");
	}
    }

    private void buildEndInputs(List<MatlabNode> inputs, MatlabNode parenthesisNode) {
	buildInputs(inputs, parenthesisNode.getChildren());
    }
}