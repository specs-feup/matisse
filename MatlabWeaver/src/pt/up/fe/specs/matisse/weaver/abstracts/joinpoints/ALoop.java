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
 * Auto-Generated class for join point ALoop
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents a loop statement
 * @author Lara Weaver Generator
 */
public abstract class ALoop extends AMWeaverJoinPoint {

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
     * Get value on attribute kind
     * @return the attribute's value
     */
    public abstract String getKindImpl();

    /**
     * Get value on attribute kind
     * @return the attribute's value
     */
    public final Object getKind() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "kind", Optional.empty());
        	}
        	String result = this.getKindImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "kind", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "kind", e);
        }
    }

    /**
     * Get value on attribute is_innermost
     * @return the attribute's value
     */
    public abstract Boolean getIs_innermostImpl();

    /**
     * Get value on attribute is_innermost
     * @return the attribute's value
     */
    public final Object getIs_innermost() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "is_innermost", Optional.empty());
        	}
        	Boolean result = this.getIs_innermostImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "is_innermost", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "is_innermost", e);
        }
    }

    /**
     * Get value on attribute is_outermost
     * @return the attribute's value
     */
    public abstract Boolean getIs_outermostImpl();

    /**
     * Get value on attribute is_outermost
     * @return the attribute's value
     */
    public final Object getIs_outermost() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "is_outermost", Optional.empty());
        	}
        	Boolean result = this.getIs_outermostImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "is_outermost", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "is_outermost", e);
        }
    }

    /**
     * Get value on attribute nestedLevel
     * @return the attribute's value
     */
    public abstract Integer getNestedLevelImpl();

    /**
     * Get value on attribute nestedLevel
     * @return the attribute's value
     */
    public final Object getNestedLevel() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "nestedLevel", Optional.empty());
        	}
        	Integer result = this.getNestedLevelImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "nestedLevel", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "nestedLevel", e);
        }
    }

    /**
     * Get value on attribute inductionVar
     * @return the attribute's value
     */
    public abstract String getInductionVarImpl();

    /**
     * Get value on attribute inductionVar
     * @return the attribute's value
     */
    public final Object getInductionVar() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "inductionVar", Optional.empty());
        	}
        	String result = this.getInductionVarImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "inductionVar", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "inductionVar", e);
        }
    }

    /**
     * Default implementation of the method used by the lara interpreter to select controls
     * @return 
     */
    public List<? extends AVar> selectControl() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select exprs
     * @return 
     */
    public List<? extends AExpression> selectExpr() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select inits
     * @return 
     */
    public List<? extends AExpression> selectInit() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select steps
     * @return 
     */
    public List<? extends AExpression> selectStep() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select conds
     * @return 
     */
    public List<? extends AExpression> selectCond() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select headers
     * @return 
     */
    public List<? extends AStatement> selectHeader() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select bodys
     * @return 
     */
    public List<? extends ABody> selectBody() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody.class, SelectOp.DESCENDANTS);
    }

    /**
     * Switches the nesting order of two for loops.
     */
    public void interchangeImpl() {
        throw new UnsupportedOperationException(get_class()+": Action interchange not implemented ");
    }

    /**
     * Switches the nesting order of two for loops.
     */
    public final void interchange() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "interchange", this, Optional.empty());
        	}
        	this.interchangeImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "interchange", this, Optional.empty());
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "interchange", e);
        }
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "control": 
        		joinPointList = selectControl();
        		break;
        	case "expr": 
        		joinPointList = selectExpr();
        		break;
        	case "init": 
        		joinPointList = selectInit();
        		break;
        	case "step": 
        		joinPointList = selectStep();
        		break;
        	case "cond": 
        		joinPointList = selectCond();
        		break;
        	case "header": 
        		joinPointList = selectHeader();
        		break;
        	case "body": 
        		joinPointList = selectBody();
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
        attributes.add("type");
        attributes.add("kind");
        attributes.add("is_innermost");
        attributes.add("is_outermost");
        attributes.add("nestedLevel");
        attributes.add("inductionVar");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("control");
        selects.add("expr");
        selects.add("init");
        selects.add("step");
        selects.add("cond");
        selects.add("header");
        selects.add("body");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        super.fillWithActions(actions);
        actions.add("void interchange()");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "loop";
    }
    /**
     * 
     */
    protected enum LoopAttributes {
        TYPE("type"),
        KIND("kind"),
        IS_INNERMOST("is_innermost"),
        IS_OUTERMOST("is_outermost"),
        NESTEDLEVEL("nestedLevel"),
        INDUCTIONVAR("inductionVar"),
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
        private LoopAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<LoopAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(LoopAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
