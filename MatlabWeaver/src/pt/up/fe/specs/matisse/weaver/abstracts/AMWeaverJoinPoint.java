package pt.up.fe.specs.matisse.weaver.abstracts;

import java.util.List;
import java.util.stream.Collectors;

import org.lara.interpreter.weaver.interf.JoinPoint;
import org.lara.interpreter.weaver.interf.SelectOp;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.xmlwriter.MatlabXmlConverter;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.matisse.weaver.MatlabJoinpoints;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AJoinPoint;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;
import pt.up.fe.specs.matisse.weaver.joinpoints.java.MApp;
import pt.up.fe.specs.matisse.weaver.utils.Action;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

/**
 * Abstract class which can be edited by the developer. This class will not be overwritten.
 *
 * @author Lara C.
 */
public abstract class AMWeaverJoinPoint extends AJoinPoint {

    // private static final AMWeaverJoinPoint INVALID_JP = new MApp(Collections.emptyList());
    private AMWeaverJoinPoint parent;
    private boolean isInitialized;

    public AMWeaverJoinPoint() {
        parent = null;
        isInitialized = false;
    }

    /**
     * Generic select function, used by the default select implementations.
     */
    @Override
    public <T extends AJoinPoint> List<? extends T> select(Class<T> joinPointClass, SelectOp op) {
        throw new RuntimeException(
                "Generic select function not implemented yet. Implement it in order to use the default implementations of select");
    }

    // public AMWeaverJoinPoint() {
    // this(null);
    // }
    //
    // public AMWeaverJoinPoint(AMWeaverJoinPoint parent) {
    // this.parent = parent;
    // }

    protected void initMWeaverJP(AMWeaverJoinPoint parent) {
        // System.out.println("INVALID JP:" + INVALID_JP);
        // System.out.println("PARENT:" + this.parent);
        // if (parent != INVALID_JP) {
        if (isInitialized) {
            throw new RuntimeException("Setting the field 'parent' for the second time");
        }

        this.parent = parent;
        isInitialized = true;
    }

    @Override
    public AMWeaverJoinPoint getParentImpl() {
        // if (parent == INVALID_JP) {
        if (!isInitialized) {
            throw new RuntimeException("Field 'parent' not initialized for class '" + getClass() + "'");
        }

        // if (parent == null) {
        // throw new RuntimeException("Method 'getParent()' not supported for joinpoint '" + getJpName() + "'");
        // }

        return parent;
    }

    public String getJpName() {
        String className = getClass().getSimpleName();

        // Assume all joinpoint classes start with 'M'
        return className.substring(1, className.length());
    }

    @Override
    public String getCodeImpl() {
        assert getNode() != null : "Null node at call to getCode for " + getClass().getSimpleName();
        return getNode().getCode();
    }

    /**
     * Insert for joinpoints that return a single Matlab node.
     */
    @Override
    public AJoinPoint[] insertImpl(String position, String code) {
        return Action.insert(getNode(), position, code);
        /*
        	if (this instanceof MatlabNodeProvider) {
        	    Action.insert(((MatlabNodeProvider) this).getNode(), position, code);
        	    return;
        	}
        
        	throw new RuntimeException("'insert' not implemented for joinpoint '" + get_class() + "'");
        	*/
    }

    @Override
    public AJoinPoint[] insertImpl(String position, JoinPoint JoinPoint) {
        throw new NotImplementedException(this);
    }

    @Override
    public void defImpl(String attribute, Object value) {
        // As default, use App def
        MJoinpointUtils.getAncestor(this, MApp.class).get().defImpl(attribute, value);
    }

    /**
     * Compares the two join points based on their node reference of the used compiler/parsing tool.<br>
     * This is the default implementation for comparing two join points. <br>
     * <b>Note for developers:</b> A weaver may override this implementation in the editable abstract join point, so the
     * changes are made for all join points, or override this method in specific join points.
     */
    @Override
    public boolean compareNodes(AJoinPoint aJoinPoint) {
        return getNode().equals(aJoinPoint.getNode());
    }

    @Override
    public Long getUidImpl() {

        int uid = getNode().hashCode();

        // Make sure all uids are positive numbers
        if (uid < 0) {
            return Long.parseLong(Integer.toBinaryString(uid), 2);
        }

        return (long) uid;

    }

    @Override
    public String getAstImpl() {
        return getNode().toString();
    }

    @Override
    public String getXmlImpl() {
        return MatlabXmlConverter.toXml(getNode());
    }

    /*
    
    @DEPRECATED
        @Override
        public boolean same(JoinPoint obj) {
    	if (!(getClass().isInstance(obj)))
    	    return false;
    	if (this.getUid() == getClass().cast(obj).getUid())
    	    return true;
    	return false;
        }
    /**/
    @Override
    public Integer getLineImpl() {
        return getNode().getLine();
    }

    @Override
    public AJoinPoint ancestorImpl(String type) {
        Preconditions.checkNotNull(type, "Missing type of ancestor in attribute 'ancestor'");

        if (type.equals("app")) {
            SpecsLogs.msgInfo("Consider using attribute .root, instead of .ancestor('app')");
        }

        MatlabNode currentNode = getNode();
        while (currentNode.hasParent()) {
            AMWeaverJoinPoint parentJp = MatlabJoinpoints.newJoinpoint(currentNode.getParent(), null);
            if (parentJp == null) {
                SpecsLogs.msgInfo("Could not get ancestor for type '" + type + "'");
                return null;
            }

            if (parentJp.getJoinPointType().equals(type)) {
                return parentJp;
            }

            currentNode = parentJp.getNode();
        }

        return null;
    }

    @Override
    public Boolean getHasParentImpl() {
        return getParent() != null;
    }

    @Override
    public void insertAfterImpl(String code) {
        insertImpl("after", code);
    }

    @Override
    public void insertBeforeImpl(String code) {
        insertImpl("before", code);
    }

    @Override
    public void insertAfterImpl(AJoinPoint node) {
        Action.insertMatlabNode(node.getNode(), getNode(), "after");
    }

    @Override
    public void insertBeforeImpl(AJoinPoint node) {
        Action.insertMatlabNode(node.getNode(), getNode(), "before");
    }

    @Override
    public AJoinPoint chainAncestorImpl(String type) {
        Preconditions.checkNotNull(type, "Missing type of ancestor in attribute 'chainAncestor'");

        if (type.equals("app")) {
            SpecsLogs.warn("Consider using attribute .root, instead of .chainAncestor('app')");
        }

        AMWeaverJoinPoint currentJp = this;
        while (currentJp.getHasParentImpl()) {
            AMWeaverJoinPoint parentJp = currentJp.getParentImpl();
            if (parentJp.instanceOf(type)) {
                return parentJp;
            }

            currentJp = parentJp;
        }

        return null;
    }

    @Override
    public AJoinPoint getRootImpl() {
        return getWeaverEngine().getApp();
    }

    @Override
    public AJoinPoint getAstParentImpl() {
        MatlabNode node = getNode();
        if (!node.hasParent()) {
            return null;
        }

        MatlabNode currentParent = node.getParent();

        return MatlabJoinpoints.newJoinpoint(currentParent, this);
    }

    @Override
    public AJoinPoint astAncestorImpl(String type) {
        Preconditions.checkNotNull(type, "Missing type of ancestor in attribute 'astAncestor'");

        MatlabNode currentNode = getNode();
        while (currentNode.hasParent()) {
            MatlabNode parentNode = currentNode.getParent();

            if (parentNode.getNodeName().equals(type)) {
                return MatlabJoinpoints.newJoinpoint(parentNode, this);
            }

            currentNode = parentNode;
        }

        return null;
    }

    @Override
    public String getAstNameImpl() {
        return getNode().getNodeName();
    }

    @Override
    public AJoinPoint astChildImpl(Integer index) {
        MatlabNode node = getNode();

        if (node == null) {
            return null;
        }

        if (index >= node.getNumChildren()) {
            SpecsLogs.warn(
                    "Index '" + index + "' is out of range, node only has " + node.getNumChildren() + " children");
            return null;
        }
        System.out.println("NODE CHILD: " + node.getChild(index).getClass());
        AJoinPoint newJp = MatlabJoinpoints.newJoinpoint(node.getChild(index), this);
        System.out.println("JP CHILD: " + newJp.getNode().getClass());
        return newJp;
    }

    @Override
    public AJoinPoint[] getDescendantsArrayImpl() {
        return getNode().getDescendantsStream()
                .map(descendant -> MatlabJoinpoints.newJoinpoint(descendant, this))
                .toArray(AJoinPoint[]::new);
    }

    @Override
    public AJoinPoint[] descendantsArrayImpl(String type) {
        Preconditions.checkNotNull(type, "Missing type of descendants in attribute 'descendants'");

        return getNode().getDescendantsStream()
                .map(descendant -> MatlabJoinpoints.newJoinpoint(descendant, this))
                .filter(jp -> jp.instanceOf(type))
                .toArray(AJoinPoint[]::new);
    }

    @Override
    public AJoinPoint[] descendantsAndSelfArrayImpl(String type) {
        Preconditions.checkNotNull(type, "Missing type of descendants in attribute 'descendants'");

        return getNode().getDescendantsAndSelfStream()
                .map(descendant -> MatlabJoinpoints.newJoinpoint(descendant, this))
                .filter(jp -> jp.instanceOf(type))
                .toArray(AJoinPoint[]::new);
    }

    @Override
    public Boolean getHasAstParentImpl() {
        return getNode().hasParent();
    }

    @Override
    public Integer getAstNumChildrenImpl() {
        MatlabNode node = getNode();
        if (node == null) {
            return -1;
        }

        return node.getNumChildren();
    }

    @Override
    public AJoinPoint[] getAstChildrenArrayImpl() {
        return getNode().getChildren().stream()
                .map(node -> MatlabJoinpoints.newJoinpoint(node, this))
                .collect(Collectors.toList())
                .toArray(new AJoinPoint[0]);
    }

    @Override
    public void detachImpl() {
        MatlabNode node = getNode();

        if (!getHasParentImpl()) {
            SpecsLogs.msgInfo(
                    "action detach: could not find a parent in joinpoint of type '" + getJoinPointType() + "'");
            return;
        }

        node.detach();
    }

}
