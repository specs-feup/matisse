package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AOperator
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class AOperator extends AExpression {

    protected AExpression aExpression;

    /**
     * 
     */
    public AOperator(AExpression aExpression){
        this.aExpression = aExpression;
    }
    /**
     * 1 for unary operators and 2 for binary operators
     */
    public abstract Integer getArityImpl();

    /**
     * 1 for unary operators and 2 for binary operators
     */
    public final Object getArity() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "arity", Optional.empty());
        	}
        	Integer result = this.getArityImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "arity", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "arity", e);
        }
    }

    /**
     * Get value on attribute symbol
     * @return the attribute's value
     */
    public abstract String getSymbolImpl();

    /**
     * Get value on attribute symbol
     * @return the attribute's value
     */
    public final Object getSymbol() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "symbol", Optional.empty());
        	}
        	String result = this.getSymbolImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "symbol", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "symbol", e);
        }
    }

    /**
     * Get value on attribute leftOperand
     * @return the attribute's value
     */
    public abstract String getLeftOperandImpl();

    /**
     * Get value on attribute leftOperand
     * @return the attribute's value
     */
    public final Object getLeftOperand() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "leftOperand", Optional.empty());
        	}
        	String result = this.getLeftOperandImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "leftOperand", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "leftOperand", e);
        }
    }

    /**
     * Get value on attribute rightOperand
     * @return the attribute's value
     */
    public abstract String getRightOperandImpl();

    /**
     * Get value on attribute rightOperand
     * @return the attribute's value
     */
    public final Object getRightOperand() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "rightOperand", Optional.empty());
        	}
        	String result = this.getRightOperandImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "rightOperand", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "rightOperand", e);
        }
    }

    /**
     * Get value on attribute operands
     * @return the attribute's value
     */
    public abstract String[] getOperandsArrayImpl();

    /**
     * Get value on attribute operands
     * @return the attribute's value
     */
    public Object getOperandsImpl() {
        String[] stringArrayImpl0 = getOperandsArrayImpl();
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(stringArrayImpl0);
        return nativeArray0;
    }

    /**
     * Get value on attribute operands
     * @return the attribute's value
     */
    public final Object getOperands() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "operands", Optional.empty());
        	}
        	Object result = this.getOperandsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "operands", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "operands", e);
        }
    }

    /**
     * Default implementation of the method used by the lara interpreter to select operands
     * @return 
     */
    public List<? extends AOperand> selectOperand() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperand.class, SelectOp.DESCENDANTS);
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
        	case "operand": 
        		joinPointList = selectOperand();
        		break;
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
        attributes.add("arity");
        attributes.add("symbol");
        attributes.add("leftOperand");
        attributes.add("rightOperand");
        attributes.add("operands");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        this.aExpression.fillWithSelects(selects);
        selects.add("operand");
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
        return "operator";
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
    protected enum OperatorAttributes {
        ARITY("arity"),
        SYMBOL("symbol"),
        LEFTOPERAND("leftOperand"),
        RIGHTOPERAND("rightOperand"),
        OPERANDS("operands"),
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
        private OperatorAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<OperatorAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(OperatorAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
