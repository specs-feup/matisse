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
 * Auto-Generated class for join point AApp
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents the entire application
 * @author Lara Weaver Generator
 */
public abstract class AApp extends AMWeaverJoinPoint {

    /**
     * 
     * @param filename
     * @return 
     */
    public abstract Boolean hasFileImpl(String filename);

    /**
     * 
     * @param filename
     * @return 
     */
    public final Object hasFile(String filename) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "hasFile", Optional.empty(), filename);
        	}
        	Boolean result = this.hasFileImpl(filename);
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "hasFile", Optional.ofNullable(result), filename);
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "hasFile", e);
        }
    }

    /**
     * Selects the files of code of the application.
     * @return 
     */
    public List<? extends AFile> selectFile() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFile.class, SelectOp.DESCENDANTS);
    }

    /**
     * Defines the type of a global variable
     * @param variable 
     * @param type 
     */
    public void setGlobalTypeImpl(String variable, String type) {
        throw new UnsupportedOperationException(get_class()+": Action setGlobalType not implemented ");
    }

    /**
     * Defines the type of a global variable
     * @param variable 
     * @param type 
     */
    public final void setGlobalType(String variable, String type) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "setGlobalType", this, Optional.empty(), variable, type);
        	}
        	this.setGlobalTypeImpl(variable, type);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "setGlobalType", this, Optional.empty(), variable, type);
        	}
        } catch(Exception e) {
        	throw new ActionException(get_class(), "setGlobalType", e);
        }
    }

    /**
     * Adds a file to the program
     * @param filename 
     * @param code 
     */
    public AJoinPoint addFileImpl(String filename, String code) {
        throw new UnsupportedOperationException(get_class()+": Action addFile not implemented ");
    }

    /**
     * Adds a file to the program
     * @param filename 
     * @param code 
     */
    public final AJoinPoint addFile(String filename, String code) {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.BEGIN, "addFile", this, Optional.empty(), filename, code);
        	}
        	AJoinPoint result = this.addFileImpl(filename, code);
        	if(hasListeners()) {
        		eventTrigger().triggerAction(Stage.END, "addFile", this, Optional.ofNullable(result), filename, code);
        	}
        	return result;
        } catch(Exception e) {
        	throw new ActionException(get_class(), "addFile", e);
        }
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "file": 
        		joinPointList = selectFile();
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
        attributes.add("hasFile");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("file");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        super.fillWithActions(actions);
        actions.add("void setGlobalType(String, String)");
        actions.add("joinpoint addFile(String, String)");
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "app";
    }
    /**
     * 
     */
    protected enum AppAttributes {
        HASFILE("hasFile"),
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
        private AppAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<AppAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(AppAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
