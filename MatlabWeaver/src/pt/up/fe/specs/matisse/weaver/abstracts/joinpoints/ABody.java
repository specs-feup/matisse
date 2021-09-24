package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import org.lara.interpreter.exception.ActionException;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point ABody
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents a block of code
 * @author Lara Weaver Generator
 */
public abstract class ABody extends AMWeaverJoinPoint {

    /**
     * Get value on attribute numberOfStatements
     * @return the attribute's value
     */
    public abstract Integer getNumberOfStatementsImpl();

    /**
     * Get value on attribute numberOfStatements
     * @return the attribute's value
     */
    public final Object getNumberOfStatements() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "numberOfStatements", Optional.empty());
        	}
        	Integer result = this.getNumberOfStatementsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "numberOfStatements", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "numberOfStatements", e);
        }
    }

    /**
     * Get value on attribute lastStatement
     * @return the attribute's value
     */
    public abstract AJoinPoint getLastStatementImpl();

    /**
     * Get value on attribute lastStatement
     * @return the attribute's value
     */
    public final Object getLastStatement() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "lastStatement", Optional.empty());
        	}
        	AJoinPoint result = this.getLastStatementImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "lastStatement", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "lastStatement", e);
        }
    }

    /**
     * Gets the first statement
     * @return 
     */
    public List<? extends AStatement> selectFirst() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select lasts
     * @return 
     */
    public List<? extends AStatement> selectLast() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select statements
     * @return 
     */
    public List<? extends AStatement> selectStatement() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select assignments
     * @return 
     */
    public List<? extends AAssignment> selectAssignment() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AAssignment.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select vars
     * @return 
     */
    public List<? extends AVar> selectVar() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select loops
     * @return 
     */
    public List<? extends ALoop> selectLoop() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ALoop.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select ifs
     * @return 
     */
    public List<? extends AIf> selectIf() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AIf.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select sections
     * @return 
     */
    public List<? extends ASection> selectSection() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ASection.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select comments
     * @return 
     */
    public List<? extends AComment> selectComment() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select calls
     * @return 
     */
    public List<? extends ACall> selectCall() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ACall.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select operators
     * @return 
     */
    public List<? extends AOperator> selectOperator() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperator.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select globalDeclarations
     * @return 
     */
    public List<? extends AGlobalDeclaration> selectGlobalDeclaration() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AGlobalDeclaration.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     * @param node 
     */
    public void insertBeginImpl(AJoinPoint node) {
        throw new UnsupportedOperationException(get_class()+": Action insertBegin not implemented ");
    }

    /**
     * 
     * @param node 
     */
    public final void insertBegin(AJoinPoint node) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertBegin", this, Optional.empty(), node);
        	}
        	this.insertBeginImpl(node);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertBegin", this, Optional.empty(), node);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertBegin", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void insertBeginImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action insertBegin not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void insertBegin(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertBegin", this, Optional.empty(), code);
        	}
        	this.insertBeginImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertBegin", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertBegin", e);
        }
    }

    /**
     * 
     * @param node 
     */
    public void insertEndImpl(AJoinPoint node) {
        throw new UnsupportedOperationException(get_class()+": Action insertEnd not implemented ");
    }

    /**
     * 
     * @param node 
     */
    public final void insertEnd(AJoinPoint node) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertEnd", this, Optional.empty(), node);
        	}
        	this.insertEndImpl(node);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertEnd", this, Optional.empty(), node);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertEnd", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void insertEndImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action insertEnd not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void insertEnd(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertEnd", this, Optional.empty(), code);
        	}
        	this.insertEndImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertEnd", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertEnd", e);
        }
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "first": 
        		joinPointList = selectFirst();
        		break;
        	case "last": 
        		joinPointList = selectLast();
        		break;
        	case "statement": 
        		joinPointList = selectStatement();
        		break;
        	case "assignment": 
        		joinPointList = selectAssignment();
        		break;
        	case "var": 
        		joinPointList = selectVar();
        		break;
        	case "loop": 
        		joinPointList = selectLoop();
        		break;
        	case "if": 
        		joinPointList = selectIf();
        		break;
        	case "section": 
        		joinPointList = selectSection();
        		break;
        	case "comment": 
        		joinPointList = selectComment();
        		break;
        	case "call": 
        		joinPointList = selectCall();
        		break;
        	case "operator": 
        		joinPointList = selectOperator();
        		break;
        	case "globalDeclaration": 
        		joinPointList = selectGlobalDeclaration();
        		break;
        	default:
        		joinPointList = super.select(selectName);
        		break;
        }
        return joinPointList;
    }

    /**
     * 
     */
    @Override
    protected final void fillWithAttributes(List<String> attributes) {
        super.fillWithAttributes(attributes);
        attributes.add("numberOfStatements");
        attributes.add("lastStatement");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("first");
        selects.add("last");
        selects.add("statement");
        selects.add("assignment");
        selects.add("var");
        selects.add("loop");
        selects.add("if");
        selects.add("section");
        selects.add("comment");
        selects.add("call");
        selects.add("operator");
        selects.add("globalDeclaration");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        super.fillWithActions(actions);
        actions.add("void insertBegin(joinpoint)");
        actions.add("void insertBegin(String)");
        actions.add("void insertEnd(joinpoint)");
        actions.add("void insertEnd(string)");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "body";
    }
    /**
     * 
     */
    protected enum BodyAttributes {
        NUMBEROFSTATEMENTS("numberOfStatements"),
        LASTSTATEMENT("lastStatement"),
        CHAINANCESTOR("chainAncestor"),
        PARENT("parent"),
        AST("ast"),
        CODE("code"),
        ASTANCESTOR("astAncestor"),
        LINE("line"),
        ANCESTOR("ancestor"),
        DESCENDANTSANDSELF("descendantsAndSelf"),
        HASASTPARENT("hasAstParent"),
        ASTNUMCHILDREN("astNumChildren"),
        ASTCHILD("astChild"),
        ASTNAME("astName"),
        DESCENDANTS("descendants"),
        ASTCHILDREN("astChildren"),
        UID("uid"),
        ASTPARENT("astParent"),
        XML("xml"),
        ROOT("root"),
        HASPARENT("hasParent");
        private String name;

        /**
         * 
         */
        private BodyAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<BodyAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(BodyAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
