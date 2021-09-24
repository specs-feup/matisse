package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import org.lara.interpreter.exception.ActionException;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point ACall
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class ACall extends AExpression {

    protected AExpression aExpression;

    /**
     * 
     */
    public ACall(AExpression aExpression){
        this.aExpression = aExpression;
    }
    /**
     * Get value on attribute name
     * @return the attribute's value
     */
    public abstract String getNameImpl();

    /**
     * Get value on attribute name
     * @return the attribute's value
     */
    public final Object getName() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "name", Optional.empty());
        	}
        	String result = this.getNameImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "name", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "name", e);
        }
    }

    /**
     * Get value on attribute type
     * @return the attribute's value
     */
    public abstract String getTypeImpl();

    /**
     * Get value on attribute type
     * @return the attribute's value
     */
    public final Object getType() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "type", Optional.empty());
        	}
        	String result = this.getTypeImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "type", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "type", e);
        }
    }

    /**
     * Get value on attribute num_args
     * @return the attribute's value
     */
    public abstract Integer getNum_argsImpl();

    /**
     * Get value on attribute num_args
     * @return the attribute's value
     */
    public final Object getNum_args() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "num_args", Optional.empty());
        	}
        	Integer result = this.getNum_argsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "num_args", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "num_args", e);
        }
    }

    /**
     * Get value on attribute arguments
     * @return the attribute's value
     */
    public abstract String[] getArgumentsArrayImpl();

    /**
     * Get value on attribute arguments
     * @return the attribute's value
     */
    public Object getArgumentsImpl() {
        String[] stringArrayImpl0 = getArgumentsArrayImpl();
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(stringArrayImpl0);
        return nativeArray0;
    }

    /**
     * Get value on attribute arguments
     * @return the attribute's value
     */
    public final Object getArguments() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "arguments", Optional.empty());
        	}
        	Object result = this.getArgumentsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "arguments", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "arguments", e);
        }
    }

    /**
     * Default implementation of the method used by the lara interpreter to select arguments
     * @return 
     */
    public List<? extends AExpression> selectArgument() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     * @param code 
     */
    public void appendArgumentImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action appendArgument not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void appendArgument(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "appendArgument", this, Optional.empty(), code);
        	}
        	this.appendArgumentImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "appendArgument", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "appendArgument", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void prependArgumentImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action prependArgument not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void prependArgument(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "prependArgument", this, Optional.empty(), code);
        	}
        	this.prependArgumentImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "prependArgument", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "prependArgument", e);
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
        	case "argument": 
        		joinPointList = selectArgument();
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
        attributes.add("name");
        attributes.add("type");
        attributes.add("num_args");
        attributes.add("arguments");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        this.aExpression.fillWithSelects(selects);
        selects.add("argument");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        this.aExpression.fillWithActions(actions);
        actions.add("void appendArgument(String)");
        actions.add("void prependArgument(String)");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "call";
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
    protected enum CallAttributes {
        NAME("name"),
        TYPE("type"),
        NUM_ARGS("num_args"),
        ARGUMENTS("arguments"),
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
        private CallAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<CallAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(CallAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
