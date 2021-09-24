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
 * Auto-Generated class for join point AFunction
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents a MATLAB function
 * @author Lara Weaver Generator
 */
public abstract class AFunction extends AMWeaverJoinPoint {

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
     * Get value on attribute ftype
     * @return the attribute's value
     */
    public abstract String getFtypeImpl();

    /**
     * Get value on attribute ftype
     * @return the attribute's value
     */
    public final Object getFtype() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "ftype", Optional.empty());
        	}
        	String result = this.getFtypeImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "ftype", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "ftype", e);
        }
    }

    /**
     * Get value on attribute qualifiedName
     * @return the attribute's value
     */
    public abstract String[] getQualifiedNameArrayImpl();

    /**
     * Get value on attribute qualifiedName
     * @return the attribute's value
     */
    public Object getQualifiedNameImpl() {
        String[] stringArrayImpl0 = getQualifiedNameArrayImpl();
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(stringArrayImpl0);
        return nativeArray0;
    }

    /**
     * Get value on attribute qualifiedName
     * @return the attribute's value
     */
    public final Object getQualifiedName() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "qualifiedName", Optional.empty());
        	}
        	Object result = this.getQualifiedNameImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "qualifiedName", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "qualifiedName", e);
        }
    }

    /**
     * Get value on attribute numberOfOutputs
     * @return the attribute's value
     */
    public abstract Integer getNumberOfOutputsImpl();

    /**
     * Get value on attribute numberOfOutputs
     * @return the attribute's value
     */
    public final Object getNumberOfOutputs() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "numberOfOutputs", Optional.empty());
        	}
        	Integer result = this.getNumberOfOutputsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "numberOfOutputs", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "numberOfOutputs", e);
        }
    }

    /**
     * Get value on attribute numberOfInputs
     * @return the attribute's value
     */
    public abstract Integer getNumberOfInputsImpl();

    /**
     * Get value on attribute numberOfInputs
     * @return the attribute's value
     */
    public final Object getNumberOfInputs() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "numberOfInputs", Optional.empty());
        	}
        	Integer result = this.getNumberOfInputsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "numberOfInputs", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "numberOfInputs", e);
        }
    }

    /**
     * Get value on attribute body
     * @return the attribute's value
     */
    public abstract AJoinPoint getBodyImpl();

    /**
     * Get value on attribute body
     * @return the attribute's value
     */
    public final Object getBody() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "body", Optional.empty());
        	}
        	AJoinPoint result = this.getBodyImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "body", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "body", e);
        }
    }

    /**
     * Get value on attribute id
     * @return the attribute's value
     */
    public abstract String getIdImpl();

    /**
     * Get value on attribute id
     * @return the attribute's value
     */
    public final Object getId() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "id", Optional.empty());
        	}
        	String result = this.getIdImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "id", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "id", e);
        }
    }

    /**
     * Default implementation of the method used by the lara interpreter to select vars
     * @return 
     */
    public List<? extends AVar> selectVar() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar.class, SelectOp.DESCENDANTS);
    }

    /**
     * Selects the arguments of the function
     * @return 
     */
    public List<? extends AVar> selectInput() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar.class, SelectOp.DESCENDANTS);
    }

    /**
     * Selects the outputs (returned variables) of the function
     * @return 
     */
    public List<? extends AVar> selectOutput() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select bodys
     * @return 
     */
    public List<? extends ABody> selectBody() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select headers
     * @return 
     */
    public List<? extends AFunctionHeader> selectHeader() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunctionHeader.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select headerComments
     * @return 
     */
    public List<? extends AComment> selectHeaderComment() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     * @param variable 
     * @param type 
     */
    public void defTypeImpl(String variable, String type) {
        throw new UnsupportedOperationException(get_class()+": Action defType not implemented ");
    }

    /**
     * 
     * @param variable 
     * @param type 
     */
    public final void defType(String variable, String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "defType", this, Optional.empty(), variable, type);
        	}
        	this.defTypeImpl(variable, type);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "defType", this, Optional.empty(), variable, type);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "defType", e);
        }
    }

    /**
     * 
     * @param name 
     */
    public void appendInputImpl(String name) {
        throw new UnsupportedOperationException(get_class()+": Action appendInput not implemented ");
    }

    /**
     * 
     * @param name 
     */
    public final void appendInput(String name) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "appendInput", this, Optional.empty(), name);
        	}
        	this.appendInputImpl(name);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "appendInput", this, Optional.empty(), name);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "appendInput", e);
        }
    }

    /**
     * 
     * @param name 
     */
    public void appendOutputImpl(String name) {
        throw new UnsupportedOperationException(get_class()+": Action appendOutput not implemented ");
    }

    /**
     * 
     * @param name 
     */
    public final void appendOutput(String name) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "appendOutput", this, Optional.empty(), name);
        	}
        	this.appendOutputImpl(name);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "appendOutput", this, Optional.empty(), name);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "appendOutput", e);
        }
    }

    /**
     * 
     * @param name 
     */
    public void prependInputImpl(String name) {
        throw new UnsupportedOperationException(get_class()+": Action prependInput not implemented ");
    }

    /**
     * 
     * @param name 
     */
    public final void prependInput(String name) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "prependInput", this, Optional.empty(), name);
        	}
        	this.prependInputImpl(name);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "prependInput", this, Optional.empty(), name);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "prependInput", e);
        }
    }

    /**
     * 
     * @param name 
     */
    public void prependOutputImpl(String name) {
        throw new UnsupportedOperationException(get_class()+": Action prependOutput not implemented ");
    }

    /**
     * 
     * @param name 
     */
    public final void prependOutput(String name) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "prependOutput", this, Optional.empty(), name);
        	}
        	this.prependOutputImpl(name);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "prependOutput", this, Optional.empty(), name);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "prependOutput", e);
        }
    }

    /**
     * 
     * @param name 
     */
    public void addGlobalImpl(String name) {
        throw new UnsupportedOperationException(get_class()+": Action addGlobal not implemented ");
    }

    /**
     * 
     * @param name 
     */
    public final void addGlobal(String name) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "addGlobal", this, Optional.empty(), name);
        	}
        	this.addGlobalImpl(name);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "addGlobal", this, Optional.empty(), name);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "addGlobal", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void insertReturnImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action insertReturn not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void insertReturn(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertReturn", this, Optional.empty(), code);
        	}
        	this.insertReturnImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertReturn", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertReturn", e);
        }
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
        	case "input": 
        		joinPointList = selectInput();
        		break;
        	case "output": 
        		joinPointList = selectOutput();
        		break;
        	case "body": 
        		joinPointList = selectBody();
        		break;
        	case "header": 
        		joinPointList = selectHeader();
        		break;
        	case "headerComment": 
        		joinPointList = selectHeaderComment();
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
        attributes.add("name");
        attributes.add("ftype");
        attributes.add("qualifiedName");
        attributes.add("numberOfOutputs");
        attributes.add("numberOfInputs");
        attributes.add("body");
        attributes.add("id");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("var");
        selects.add("input");
        selects.add("output");
        selects.add("body");
        selects.add("header");
        selects.add("headerComment");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        super.fillWithActions(actions);
        actions.add("void defType(String, String)");
        actions.add("void appendInput(String)");
        actions.add("void appendOutput(String)");
        actions.add("void prependInput(String)");
        actions.add("void prependOutput(String)");
        actions.add("void addGlobal(String)");
        actions.add("void insertReturn(String)");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "function";
    }
    /**
     * 
     */
    protected enum FunctionAttributes {
        NAME("name"),
        FTYPE("ftype"),
        QUALIFIEDNAME("qualifiedName"),
        NUMBEROFOUTPUTS("numberOfOutputs"),
        NUMBEROFINPUTS("numberOfInputs"),
        BODY("body"),
        ID("id"),
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
        private FunctionAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<FunctionAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(FunctionAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
