package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import org.lara.interpreter.weaver.interf.events.Stage;
import java.util.Optional;
import org.lara.interpreter.exception.AttributeException;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import java.util.List;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point ASection
 * This class is overwritten by the Weaver Generator.
 * 
 * 
 * @author Lara Weaver Generator
 */
public abstract class ASection extends AMWeaverJoinPoint {

    /**
     * Get value on attribute label
     * @return the attribute's value
     */
    public abstract String getLabelImpl();

    /**
     * Get value on attribute label
     * @return the attribute's value
     */
    public final Object getLabel() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "label", Optional.empty());
        	}
        	String result = this.getLabelImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "label", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "label", e);
        }
    }

    /**
     * Get value on attribute args
     * @return the attribute's value
     */
    public abstract String getArgsImpl();

    /**
     * Get value on attribute args
     * @return the attribute's value
     */
    public final Object getArgs() {
        try {
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.BEGIN, this, "args", Optional.empty());
        	}
        	String result = this.getArgsImpl();
        	if(hasListeners()) {
        		eventTrigger().triggerAttribute(Stage.END, this, "args", Optional.ofNullable(result));
        	}
        	return result!=null?result:getUndefinedValue();
        } catch(Exception e) {
        	throw new AttributeException(get_class(), "args", e);
        }
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
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
        attributes.add("label");
        attributes.add("args");
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
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
        return "section";
    }
    /**
     * 
     */
    protected enum SectionAttributes {
        LABEL("label"),
        ARGS("args"),
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
        private SectionAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<SectionAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(SectionAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
