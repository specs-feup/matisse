package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import pt.up.fe.specs.matisse.weaver.entities.Sym;
import java.util.List;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AVar
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class AVar extends AExpression {

    protected AExpression aExpression;

    /**
     * 
     */
    public AVar(AExpression aExpression){
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
     * Get value on attribute reference
     * @return the attribute's value
     */
    public abstract String getReferenceImpl();

    /**
     * Get value on attribute reference
     * @return the attribute's value
     */
    public final Object getReference() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "reference", Optional.empty());
        	}
        	String result = this.getReferenceImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "reference", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "reference", e);
        }
    }

    /**
     * Get value on attribute is_read
     * @return the attribute's value
     */
    public abstract Boolean getIs_readImpl();

    /**
     * Get value on attribute is_read
     * @return the attribute's value
     */
    public final Object getIs_read() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "is_read", Optional.empty());
        	}
        	Boolean result = this.getIs_readImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "is_read", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "is_read", e);
        }
    }

    /**
     * Get value on attribute is_write
     * @return the attribute's value
     */
    public abstract Boolean getIs_writeImpl();

    /**
     * Get value on attribute is_write
     * @return the attribute's value
     */
    public final Object getIs_write() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "is_write", Optional.empty());
        	}
        	Boolean result = this.getIs_writeImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "is_write", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "is_write", e);
        }
    }

    /**
     * Get value on attribute isRead
     * @return the attribute's value
     */
    public abstract Boolean getIsReadImpl();

    /**
     * Get value on attribute isRead
     * @return the attribute's value
     */
    public final Object getIsRead() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "isRead", Optional.empty());
        	}
        	Boolean result = this.getIsReadImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "isRead", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "isRead", e);
        }
    }

    /**
     * Get value on attribute isWrite
     * @return the attribute's value
     */
    public abstract Boolean getIsWriteImpl();

    /**
     * Get value on attribute isWrite
     * @return the attribute's value
     */
    public final Object getIsWrite() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "isWrite", Optional.empty());
        	}
        	Boolean result = this.getIsWriteImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "isWrite", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "isWrite", e);
        }
    }

    /**
     * Get value on attribute isInsideLoopHeader
     * @return the attribute's value
     */
    public abstract Boolean getIsInsideLoopHeaderImpl();

    /**
     * Get value on attribute isInsideLoopHeader
     * @return the attribute's value
     */
    public final Object getIsInsideLoopHeader() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "isInsideLoopHeader", Optional.empty());
        	}
        	Boolean result = this.getIsInsideLoopHeaderImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "isInsideLoopHeader", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "isInsideLoopHeader", e);
        }
    }

    /**
     * Get value on attribute sym
     * @return the attribute's value
     */
    public abstract Sym getSymImpl();

    /**
     * Get value on attribute sym
     * @return the attribute's value
     */
    public final Object getSym() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "sym", Optional.empty());
        	}
        	Sym result = this.getSymImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "sym", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "sym", e);
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
        attributes.add("name");
        attributes.add("reference");
        attributes.add("is_read");
        attributes.add("is_write");
        attributes.add("isRead");
        attributes.add("isWrite");
        attributes.add("isInsideLoopHeader");
        attributes.add("sym");
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
        return "var";
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
    protected enum VarAttributes {
        NAME("name"),
        REFERENCE("reference"),
        IS_READ("is_read"),
        IS_WRITE("is_write"),
        ISREAD("isRead"),
        ISWRITE("isWrite"),
        ISINSIDELOOPHEADER("isInsideLoopHeader"),
        SYM("sym"),
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
        private VarAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<VarAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(VarAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
