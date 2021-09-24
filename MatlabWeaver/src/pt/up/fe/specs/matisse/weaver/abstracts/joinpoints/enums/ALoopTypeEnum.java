package pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.enums;

import org.lara.interpreter.weaver.interf.NamedEnum;

/**
 * 
 */
public enum ALoopTypeEnum  implements NamedEnum{
    FOR("for"),
    PARFOR("parfor"),
    WHILE("while");
    private String name;

    /**
     * 
     */
    private ALoopTypeEnum(String name){
        this.name = name;
    }
    /**
     * 
     */
    public String getName() {
        return name;
    }
}
