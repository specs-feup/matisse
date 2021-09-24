package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums;

import org.lara.interpreter.weaver.interf.NamedEnum;

/**
 * 
 */
public enum AFunctionFtypeEnum  implements NamedEnum{
    MAIN_FUNCTION("main_function"),
    SUB_FUNCTION("sub_function"),
    NESTED_FUNCTION("nested_function");
    private String name;

    /**
     * 
     */
    private AFunctionFtypeEnum(String name){
        this.name = name;
    }
    /**
     * 
     */
    public String getName() {
        return name;
    }
}
