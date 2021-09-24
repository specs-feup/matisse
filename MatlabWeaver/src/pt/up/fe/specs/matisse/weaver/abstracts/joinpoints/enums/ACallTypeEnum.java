package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums;

import org.lara.interpreter.weaver.interf.NamedEnum;

/**
 * 
 */
public enum ACallTypeEnum  implements NamedEnum{
    CONVENTIONAL("conventional"),
    SCRIPT("script"),
    IMPLICIT("implicit");
    private String name;

    /**
     * 
     */
    private ACallTypeEnum(String name){
        this.name = name;
    }
    /**
     * 
     */
    public String getName() {
        return name;
    }
}
