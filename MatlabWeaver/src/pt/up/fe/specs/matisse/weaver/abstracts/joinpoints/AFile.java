package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AFile
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents a file of code
 * @author Lara Weaver Generator
 */
public abstract class AFile extends AMWeaverJoinPoint {

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
     * Get value on attribute absolutePath
     * @return the attribute's value
     */
    public abstract String getAbsolutePathImpl();

    /**
     * Get value on attribute absolutePath
     * @return the attribute's value
     */
    public final Object getAbsolutePath() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "absolutePath", Optional.empty());
        	}
        	String result = this.getAbsolutePathImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "absolutePath", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "absolutePath", e);
        }
    }

    /**
     * Get value on attribute mainFunction
     * @return the attribute's value
     */
    public abstract String getMainFunctionImpl();

    /**
     * Get value on attribute mainFunction
     * @return the attribute's value
     */
    public final Object getMainFunction() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "mainFunction", Optional.empty());
        	}
        	String result = this.getMainFunctionImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "mainFunction", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "mainFunction", e);
        }
    }

    /**
     * Get value on attribute mainFunctionJp
     * @return the attribute's value
     */
    public abstract AJoinPoint getMainFunctionJpImpl();

    /**
     * Get value on attribute mainFunctionJp
     * @return the attribute's value
     */
    public final Object getMainFunctionJp() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "mainFunctionJp", Optional.empty());
        	}
        	AJoinPoint result = this.getMainFunctionJpImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "mainFunctionJp", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "mainFunctionJp", e);
        }
    }

    /**
     * Selects the functions in this file
     * @return 
     */
    public List<? extends AFunction> selectFunction() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AFunction.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select sections
     * @return 
     */
    public List<? extends ASection> selectSection() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ASection.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select comments
     * @return 
     */
    public List<? extends AComment> selectComment() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AComment.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "function": 
        		joinPointList = selectFunction();
        		break;
        	case "section": 
        		joinPointList = selectSection();
        		break;
        	case "comment": 
        		joinPointList = selectComment();
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
        attributes.add("absolutePath");
        attributes.add("mainFunction");
        attributes.add("mainFunctionJp");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("function");
        selects.add("section");
        selects.add("comment");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithActions(List<String> actions) {
        super.fillWithActions(actions);
    }

    /**
     * Returns the join point type of this class
     * @return The join point type
     */
    @Override
    public final String get_class() {
        return "file";
    }
    /**
     * 
     */
    protected enum FileAttributes {
        NAME("name"),
        ABSOLUTEPATH("absolutePath"),
        MAINFUNCTION("mainFunction"),
        MAINFUNCTIONJP("mainFunctionJp"),
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
        private FileAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<FileAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(FileAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
