package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import java.util.List;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AOperand
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class AOperand extends AExpression {

    protected AExpression aExpression;

    /**
     * 
     */
    public AOperand(AExpression aExpression){
        this.aExpression = aExpression;
    }
    /**
     * Get value on attribute index
     * @return the attribute's value
     */
    public abstract Integer getIndexImpl();

    /**
     * Get value on attribute index
     * @return the attribute's value
     */
    public final Object getIndex() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "index", Optional.empty());
        	}
        	Integer result = this.getIndexImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "index", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "index", e);
        }
    }

    /**
     * Get value on attribute value
     * @return the attribute's value
     */
    @Override
    public String getValueImpl() {
        return this.aExpression.getValueImpl();
    }

    /**
     * Method used by the lara interpreter to select vars
     * @return 
     */
    @Override
    public List<? extends AVar> selectVar() {
        return this.aExpression.selectVar();
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertBeforeImpl(AJoinPoint node) {
        this.aExpression.insertBeforeImpl(node);
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertBeforeImpl(String node) {
        this.aExpression.insertBeforeImpl(node);
    }

    /**
     * 
     * @param node 
     */
    @Override
    public void insertAfterImpl(AJoinPoint node) {
        this.aExpression.insertAfterImpl(node);
    }

    /**
     * 
     * @param code 
     */
    @Override
    public void insertAfterImpl(String code) {
        this.aExpression.insertAfterImpl(code);
    }

    /**
     * Removes the node associated to this joinpoint from the AST
     */
    @Override
    public void detachImpl() {
        this.aExpression.detachImpl();
    }

    /**
     * 
     * @param position 
     * @param code 
     */
    @Override
    public AJoinPoint[] insertImpl(String position, String code) {
        return this.aExpression.insertImpl(position, code);
    }

    /**
     * 
     * @param position 
     * @param code 
     */
    @Override
    public AJoinPoint[] insertImpl(String position, JoinPoint code) {
        return this.aExpression.insertImpl(position, code);
    }

    /**
     * 
     * @param attribute 
     * @param value 
     */
    @Override
    public void defImpl(String attribute, Object value) {
        this.aExpression.defImpl(attribute, value);
    }

    /**
     * 
     */
    @Override
    public String toString() {
        return this.aExpression.toString();
    }

    /**
     * 
     */
    @Override
    public Optional<? extends AExpression> getSuper() {
        return Optional.of(this.aExpression);
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "var": 
        		joinPointList = selectVar();
        		break;
        	default:
        		joinPointList = this.aExpression.select(selectName);
        		break;
        }
        return joinPointList;
    }

    /**
     * 
     */
    @Override
    protected final void fillWithAttributes(List<String> attributes) {
        this.aExpression.fillWithAttributes(attributes);
        attributes.add("index");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        this.aExpression.fillWithSelects(selects);
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        this.aExpression.fillWithActions(actions);
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "operand";
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
        return this.aExpression.instanceOf(joinpointClass);
    }
    /**
     * 
     */
    protected enum OperandAttributes {
        INDEX("index"),
        VALUE("value"),
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
        private OperandAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<OperandAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(OperandAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
