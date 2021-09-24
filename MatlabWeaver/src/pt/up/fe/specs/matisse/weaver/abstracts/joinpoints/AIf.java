package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints;

import java.util.List;
import org.lara.interpreter.weaver.interf.SelectOp;
import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import org.lara.interpreter.weaver.interf.JoinPoint;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Auto-Generated class for join point AIf
 * This class is overwritten by the Weaver Generator.
 * 
 * Represents an if statement
 * @author Lara Weaver Generator
 */
public abstract class AIf extends AMWeaverJoinPoint {

    /**
     * Default implementation of the method used by the lara interpreter to select headers
     * @return 
     */
    public List<? extends AStatement> selectHeader() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AStatement.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select conditions
     * @return 
     */
    public List<? extends AExpression> selectCondition() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AExpression.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select thens
     * @return 
     */
    public List<? extends ABody> selectThen() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.ABody.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select elseifs
     * @return 
     */
    public List<? extends AElseif> selectElseif() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElseif.class, SelectOp.DESCENDANTS);
    }

    /**
     * Default implementation of the method used by the lara interpreter to select elses
     * @return 
     */
    public List<? extends AElse> selectElse() {
        return select(pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AElse.class, SelectOp.DESCENDANTS);
    }

    /**
     * 
     */
    @Override
    public final List<? extends JoinPoint> select(String selectName) {
        List<? extends JoinPoint> joinPointList;
        switch(selectName) {
        	case "header": 
        		joinPointList = selectHeader();
        		break;
        	case "condition": 
        		joinPointList = selectCondition();
        		break;
        	case "then": 
        		joinPointList = selectThen();
        		break;
        	case "elseif": 
        		joinPointList = selectElseif();
        		break;
        	case "else": 
        		joinPointList = selectElse();
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
    }

    /**
     * 
     */
    @Override
    protected final void fillWithSelects(List<String> selects) {
        super.fillWithSelects(selects);
        selects.add("header");
        selects.add("condition");
        selects.add("then");
        selects.add("elseif");
        selects.add("else");
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
        return "if";
    }
    /**
     * 
     */
    protected enum IfAttributes {
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
        private IfAttributes(String name){
            this.name = name;
        }
        /**
         * Return an attribute enumeration item from a given attribute name
         */
        public static Optional<IfAttributes> fromString(String name) {
            return Arrays.asList(values()).stream().filter(attr -> attr.name.equals(name)).findAny();
        }

        /**
         * Return a list of attributes in String format
         */
        public static List<String> getNames() {
            return Arrays.asList(values()).stream().map(IfAttributes::name).collect(Collectors.toList());
        }

        /**
         * True if the enum contains the given attribute name, false otherwise.
         */
        public static boolean contains(String name) {
            return fromString(name).isPresent();
        }
    }
}
