package pt.up.fe.specs.matisse.weaver.entities;


/**
 * 
 * 
 * @author Lara C.
 */
public class Sym extends Object {

    private String name;
    private String type;

    /**
     * 
     */
    public Sym(){}
    /**
     * 
     */
    public Sym(String name, String type){
        this.setName(name);
        this.setType(type);
    }
    /**
     * Get value on attribute name
     * @return the attribute's value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set value on attribute name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get value on attribute type
     * @return the attribute's value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set value on attribute type
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     */
    @Override
    public String toString() {
        String json = "{\n";
        json += " name: "+getName() + ",\n";
        json += " type: "+getType() + ",\n";
        json+="}";
        return json;
    }
}
