package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.ActionException;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AAssignment
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class AAssignment extends AStatement {

    protected AStatement aStatement;

    /**
     * 
     */
    public AAssignment(AStatement aStatement){
        this.aStatement = aStatement;
    }
    /**
     * Default implementation of the method used by the lara interpreter to select lhss
     * @return 
     */
    public List<? extends AExpression> selectLhs() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select rhss
     * @return 
     */
    public List<? extends AExpression> selectRhs() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     * @param code 
     */
    public void appendOutputImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action appendOutput not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void appendOutput(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "appendOutput", this, Optional.empty(), code);
        	}
        	this.appendOutputImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "appendOutput", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "appendOutput", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void prependOutputImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action prependOutput not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void prependOutput(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "prependOutput", this, Optional.empty(), code);
        	}
        	this.prependOutputImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "prependOutput", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "prependOutput", e);
        }
    }

    /**
     * Get value on attribute type
     * @return the attribute's value
     */
    @Override
    public String getTypeImpl() {
        return this.aStatement.getTypeImpl();
    }

    /**
     * Get value on attribute display
     * @return the attribute's value
     */
    @Override
    public Boolean getDisplayImpl() {
        return this.aStatement.getDisplayImpl();
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertBeforeImpl(AJoinPoint node) {
        this.aStatement.insertBeforeImpl(node);
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertBeforeImpl(String node) {
        this.aStatement.insertBeforeImpl(node);
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertAfterImpl(AJoinPoint node) {
        this.aStatement.insertAfterImpl(node);
    }

    /**
     * 
     * @param code 
     */
    @Override
    public void insertAfterImpl(String code) {
        this.aStatement.insertAfterImpl(code);
    }

    /**
     * Removes the node associated to this joinpoint from the AST
     */
    @Override
    public void detachImpl() {
        this.aStatement.detachImpl();
    }

    /**
     * 
     * @param position 
     * @param code 
     */
    @Override
    public AJoinPoint[] insertImpl(String position, String code) {
        return this.aStatement.insertImpl(position, code);
    }

    /**
     * 
     * @param position 
     * @param code 
     */
    @Override
    public AJoinPoint[] insertImpl(String position, JoinPoint code) {
        return this.aStatement.insertImpl(position, code);
    }

    /**
     * 
     * @param attribute 
     * @param value 
     */
    @Override
    public void defImpl(String attribute, Object value) {
        this.aStatement.defImpl(attribute, value);
    }

    /**
     * 
     */
    @Override
    public String toString() {
        return this.aStatement.toString();
    }

    /**
     * 
     */
    @Override
    public Optional<? extends AStatement> getSuper() {
        return Optional.of(this.aStatement);
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "lhs": 
        		joinPointList = selectLhs();
        		break;
        	case "rhs": 
        		joinPointList = selectRhs();
        		break;
        	default:
        		joinPointList = this.aStatement.select(selectName);
        		break;
        }
        return joinPointList;
    }

    /**
     * 
     */
    @Override
    protected final void fillWithAttributes(List<String> attributes) {
        this.aStatement.fillWithAttributes(attributes);
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        this.aStatement.fillWithSelects(selects);
        selects.add("lhs");
        selects.add("rhs");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        this.aStatement.fillWithActions(actions);
        actions.add("void appendOutput(String)");
        actions.add("void prependOutput(String)");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "assignment";
    }

    /**
     * Defines if this joinpoint is an instanceof a given joinpoint class
     * @return True if this join point is an instanceof the given class
     */
    @Override
    public final boolean instanceOf(String joinpointClass) {
        boolean isInstance = get_class().equals(joinpointClass);
        if(isInstance) {
        	return true;
        }
        return this.aStatement.instanceOf(joinpointClass);
    }
    /**
     * 
     */
    protected enum AssignmentAttributes {
        TYPE("type"),
        DISPLAY("display"),
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
        private AssignmentAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<AssignmentAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(AssignmentAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
