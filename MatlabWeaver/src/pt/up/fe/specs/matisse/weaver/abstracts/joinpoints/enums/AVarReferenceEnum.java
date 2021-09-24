package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums;

import org.lara.interpreter.weaver.interf.NamedEnum;

/**
 * 
 */
public enum AVarReferenceEnum  implements NamedEnum{
    READ("read"),
    WRITE("write"),
    ARGUMENT("argument");
    private String name;

    /**
     * 
     */
    private AVarReferenceEnum(String name){
        this.name = name;
    }
    /**
     * 
     */
    public String getName() {
        return name;
    }
}
