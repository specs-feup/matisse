package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.JoinPoint;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import java.util.List;
import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.ActionException;
import org.lara.interpreter.exception.AttributeException;
import pt.up.fe.specs.matisse.weaver.MWeaver;
import org.lara.interpreter.weaver.interf.SelectOp;

/**
 * Abstract class containing the global attributes and default action exception.
 * This class is overwritten when the weaver generator is executed.
 * @author Lara Weaver Generator
 */
public abstract class AJoinPoint extends JoinPoint {

    /**
     * 
     */
    @Override
    public boolean same(JoinPoint iJoinPoint) {
        if (this.get_class().equals(iJoinPoint.get_class())) {
        
                return this.compareNodes((AJoinPoint) iJoinPoint);
            }
            return false;
    }

    /**
     * Compares the two join points based on their node reference of the used compiler/parsing tool.<br>
     * This is the default implementation for comparing two join points. <br>
     * <b>Note for developers:</b> A weaver may override this implementation in the editable abstract join point, so
     * the changes are made for all join points, or override this method in specific join points.
     */
    public boolean compareNodes(AJoinPoint aJoinPoint) {
        return this.getNode().equals(aJoinPoint.getNode());
    }

    /**
     * Returns the tree node reference of this join point.<br><b>NOTE</b>This method is essentially used to compare two join points
     * @return Tree node reference
     */
    public abstract MatlabNode getNode();

    /**
     * 
     */
    @Override
    protected void fillWithActions(List<String> actions) {
        actions.add("insertBefore(AJoinPoint node)");
        actions.add("insertBefore(String node)");
        actions.add("insertAfter(AJoinPoint node)");
        actions.add("insertAfter(String code)");
        actions.add("detach()");
    }

    /**
     * 
     * @param node 
     */
    public void insertBeforeImpl(AJoinPoint node) {
        throw new UnsupportedOperationException(get_class()+": Action insertBefore not implemented ");
    }

    /**
     * 
     * @param node 
     */
    public final void insertBefore(AJoinPoint node) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertBefore", this, Optional.empty(), node);
        	}
        	this.insertBeforeImpl(node);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertBefore", this, Optional.empty(), node);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertBefore", e);
        }
    }

    /**
     * 
     * @param node 
     */
    public void insertBeforeImpl(String node) {
        throw new UnsupportedOperationException(get_class()+": Action insertBefore not implemented ");
    }

    /**
     * 
     * @param node 
     */
    public final void insertBefore(String node) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertBefore", this, Optional.empty(), node);
        	}
        	this.insertBeforeImpl(node);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertBefore", this, Optional.empty(), node);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertBefore", e);
        }
    }

    /**
     * 
     * @param node 
     */
    public void insertAfterImpl(AJoinPoint node) {
        throw new UnsupportedOperationException(get_class()+": Action insertAfter not implemented ");
    }

    /**
     * 
     * @param node 
     */
    public final void insertAfter(AJoinPoint node) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertAfter", this, Optional.empty(), node);
        	}
        	this.insertAfterImpl(node);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertAfter", this, Optional.empty(), node);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertAfter", e);
        }
    }

    /**
     * 
     * @param code 
     */
    public void insertAfterImpl(String code) {
        throw new UnsupportedOperationException(get_class()+": Action insertAfter not implemented ");
    }

    /**
     * 
     * @param code 
     */
    public final void insertAfter(String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "insertAfter", this, Optional.empty(), code);
        	}
        	this.insertAfterImpl(code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "insertAfter", this, Optional.empty(), code);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "insertAfter", e);
        }
    }

    /**
     * Removes the node associated to this joinpoint from the AST
     */
    public void detachImpl() {
        throw new UnsupportedOperationException(get_class()+": Action detach not implemented ");
    }

    /**
     * Removes the node associated to this joinpoint from the AST
     */
    public final void detach() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "detach", this, Optional.empty());
        	}
        	this.detachImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "detach", this, Optional.empty());
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "detach", e);
        }
    }

    /**
     * 
     */
    @Override
    protected void fillWithAttributes(List<String> attributes) {
        //Attributes available for all join points
        attributes.add("uid");
        attributes.add("ast");
        attributes.add("xml");
        attributes.add("code");
        attributes.add("line");
        attributes.add("ancestor(String type)");
        attributes.add("parent");
        attributes.add("hasParent");
        attributes.add("chainAncestor(String type)");
        attributes.add("root");
        attributes.add("astParent");
        attributes.add("astAncestor(String type)");
        attributes.add("astName");
        attributes.add("astChild(Integer index)");
        attributes.add("descendants");
        attributes.add("descendants(String type)");
        attributes.add("descendantsAndSelf(String type)");
        attributes.add("hasAstParent");
        attributes.add("astNumChildren");
        attributes.add("astChildren");
    }

    /**
     * Get value on attribute uid
     * @return the attribute's value
     */
    public abstract Long getUidImpl();

    /**
     * Get value on attribute uid
     * @return the attribute's value
     */
    public final Object getUid() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "uid", Optional.empty());
        	}
        	Long result = this.getUidImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "uid", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "uid", e);
        }
    }

    /**
     * Get value on attribute ast
     * @return the attribute's value
     */
    public abstract String getAstImpl();

    /**
     * Get value on attribute ast
     * @return the attribute's value
     */
    public final Object getAst() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "ast", Optional.empty());
        	}
        	String result = this.getAstImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "ast", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "ast", e);
        }
    }

    /**
     * Get value on attribute xml
     * @return the attribute's value
     */
    public abstract String getXmlImpl();

    /**
     * Get value on attribute xml
     * @return the attribute's value
     */
    public final Object getXml() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "xml", Optional.empty());
        	}
        	String result = this.getXmlImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "xml", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "xml", e);
        }
    }

    /**
     * Get value on attribute code
     * @return the attribute's value
     */
    public abstract String getCodeImpl();

    /**
     * Get value on attribute code
     * @return the attribute's value
     */
    public final Object getCode() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "code", Optional.empty());
        	}
        	String result = this.getCodeImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "code", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "code", e);
        }
    }

    /**
     * Get value on attribute line
     * @return the attribute's value
     */
    public abstract Integer getLineImpl();

    /**
     * Get value on attribute line
     * @return the attribute's value
     */
    public final Object getLine() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "line", Optional.empty());
        	}
        	Integer result = this.getLineImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "line", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "line", e);
        }
    }

    /**
     * 
     * @param type
     * @return 
     */
    public abstract AJoinPoint ancestorImpl(String type);

    /**
     * 
     * @param type
     * @return 
     */
    public final Object ancestor(String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "ancestor", Optional.empty(), type);
        	}
        	AJoinPoint result = this.ancestorImpl(type);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "ancestor", Optional.ofNullable(result), type);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "ancestor", e);
        }
    }

    /**
     * Get value on attribute parent
     * @return the attribute's value
     */
    public abstract AJoinPoint getParentImpl();

    /**
     * Get value on attribute parent
     * @return the attribute's value
     */
    public final Object getParent() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "parent", Optional.empty());
        	}
        	AJoinPoint result = this.getParentImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "parent", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "parent", e);
        }
    }

    /**
     * Get value on attribute hasParent
     * @return the attribute's value
     */
    public abstract Boolean getHasParentImpl();

    /**
     * Get value on attribute hasParent
     * @return the attribute's value
     */
    public final Object getHasParent() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "hasParent", Optional.empty());
        	}
        	Boolean result = this.getHasParentImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "hasParent", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "hasParent", e);
        }
    }

    /**
     * 
     * @param type
     * @return 
     */
    public abstract AJoinPoint chainAncestorImpl(String type);

    /**
     * 
     * @param type
     * @return 
     */
    public final Object chainAncestor(String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "chainAncestor", Optional.empty(), type);
        	}
        	AJoinPoint result = this.chainAncestorImpl(type);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "chainAncestor", Optional.ofNullable(result), type);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "chainAncestor", e);
        }
    }

    /**
     * Get value on attribute root
     * @return the attribute's value
     */
    public abstract AJoinPoint getRootImpl();

    /**
     * Get value on attribute root
     * @return the attribute's value
     */
    public final Object getRoot() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "root", Optional.empty());
        	}
        	AJoinPoint result = this.getRootImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "root", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "root", e);
        }
    }

    /**
     * Get value on attribute astParent
     * @return the attribute's value
     */
    public abstract AJoinPoint getAstParentImpl();

    /**
     * Get value on attribute astParent
     * @return the attribute's value
     */
    public final Object getAstParent() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astParent", Optional.empty());
        	}
        	AJoinPoint result = this.getAstParentImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astParent", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astParent", e);
        }
    }

    /**
     * 
     * @param type
     * @return 
     */
    public abstract AJoinPoint astAncestorImpl(String type);

    /**
     * 
     * @param type
     * @return 
     */
    public final Object astAncestor(String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astAncestor", Optional.empty(), type);
        	}
        	AJoinPoint result = this.astAncestorImpl(type);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astAncestor", Optional.ofNullable(result), type);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astAncestor", e);
        }
    }

    /**
     * Get value on attribute astName
     * @return the attribute's value
     */
    public abstract String getAstNameImpl();

    /**
     * Get value on attribute astName
     * @return the attribute's value
     */
    public final Object getAstName() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astName", Optional.empty());
        	}
        	String result = this.getAstNameImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astName", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astName", e);
        }
    }

    /**
     * 
     * @param index
     * @return 
     */
    public abstract AJoinPoint astChildImpl(Integer index);

    /**
     * 
     * @param index
     * @return 
     */
    public final Object astChild(Integer index) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astChild", Optional.empty(), index);
        	}
        	AJoinPoint result = this.astChildImpl(index);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astChild", Optional.ofNullable(result), index);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astChild", e);
        }
    }

    /**
     * Get value on attribute descendants
     * @return the attribute's value
     */
    public abstract AJoinPoint[] getDescendantsArrayImpl();

    /**
     * Retrieves all descendants of the join point
     */
    public Object getDescendantsImpl() {
        AJoinPoint[] aJoinPointArrayImpl0 = getDescendantsArrayImpl();
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(aJoinPointArrayImpl0);
        return nativeArray0;
    }

    /**
     * Retrieves all descendants of the join point
     */
    public final Object getDescendants() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "descendants", Optional.empty());
        	}
        	Object result = this.getDescendantsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "descendants", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "descendants", e);
        }
    }

    /**
     * 
     * @param type
     * @return 
     */
    public abstract AJoinPoint[] descendantsArrayImpl(String type);

    /**
     * 
     * @param type
     * @return 
     */
    public Object descendantsImpl(String type) {
        AJoinPoint[] aJoinPointArrayImpl0 = descendantsArrayImpl(type);
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(aJoinPointArrayImpl0);
        return nativeArray0;
    }

    /**
     * 
     * @param type
     * @return 
     */
    public final Object descendants(String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "descendants", Optional.empty(), type);
        	}
        	Object result = this.descendantsImpl(type);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "descendants", Optional.ofNullable(result), type);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "descendants", e);
        }
    }

    /**
     * 
     * @param type
     * @return 
     */
    public abstract AJoinPoint[] descendantsAndSelfArrayImpl(String type);

    /**
     * 
     * @param type
     * @return 
     */
    public Object descendantsAndSelfImpl(String type) {
        AJoinPoint[] aJoinPointArrayImpl0 = descendantsAndSelfArrayImpl(type);
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(aJoinPointArrayImpl0);
        return nativeArray0;
    }

    /**
     * 
     * @param type
     * @return 
     */
    public final Object descendantsAndSelf(String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "descendantsAndSelf", Optional.empty(), type);
        	}
        	Object result = this.descendantsAndSelfImpl(type);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "descendantsAndSelf", Optional.ofNullable(result), type);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "descendantsAndSelf", e);
        }
    }

    /**
     * Get value on attribute hasAstParent
     * @return the attribute's value
     */
    public abstract Boolean getHasAstParentImpl();

    /**
     * Get value on attribute hasAstParent
     * @return the attribute's value
     */
    public final Object getHasAstParent() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "hasAstParent", Optional.empty());
        	}
        	Boolean result = this.getHasAstParentImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "hasAstParent", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "hasAstParent", e);
        }
    }

    /**
     * Get value on attribute astNumChildren
     * @return the attribute's value
     */
    public abstract Integer getAstNumChildrenImpl();

    /**
     * Get value on attribute astNumChildren
     * @return the attribute's value
     */
    public final Object getAstNumChildren() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astNumChildren", Optional.empty());
        	}
        	Integer result = this.getAstNumChildrenImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astNumChildren", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astNumChildren", e);
        }
    }

    /**
     * Get value on attribute astChildren
     * @return the attribute's value
     */
    public abstract AJoinPoint[] getAstChildrenArrayImpl();

    /**
     * Get value on attribute astChildren
     * @return the attribute's value
     */
    public Object getAstChildrenImpl() {
        AJoinPoint[] aJoinPointArrayImpl0 = getAstChildrenArrayImpl();
        Object nativeArray0 = getWeaverEngine().getScriptEngine().toNativeArray(aJoinPointArrayImpl0);
        return nativeArray0;
    }

    /**
     * Get value on attribute astChildren
     * @return the attribute's value
     */
    public final Object getAstChildren() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "astChildren", Optional.empty());
        	}
        	Object result = this.getAstChildrenImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "astChildren", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "astChildren", e);
        }
    }

    /**
     * Defines if this joinpoint is an instanceof a given joinpoint class
     * @return True if this join point is an instanceof the given class
     */
    @Override
    public boolean instanceOf(String joinpointClass) {
        boolean isInstance = get_class().equals(joinpointClass);
        if(isInstance) {
        	return true;
        }
        return super.instanceOf(joinpointClass);
    }

    /**
     * Returns the Weaving Engine this join point pertains to.
     */
    @Override
    public MWeaver getWeaverEngine() {
        return MWeaver.getMWeaver();
    }

    /**
     * Generic select function, used by the default select implementations.
     */
    public abstract <T extends AJoinPoint> List<? extends T> select(Class<T> joinPointClass, SelectOp op);
}
