package pt.up.fe.specs.matisse.weaver.abstracts.weaver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lara.interpreter.weaver.LaraWeaverEngine;

import pt.up.fe.specs.matisse.weaver.entities.Sym;

/**
 * Abstract Weaver Implementation for MWeaver<br>
 * Since the generated abstract classes are always overwritten, their implementation should be done by extending those
 * abstract classes with user-defined classes.<br>
 * The abstract class {@link pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint} can be used to add user-defined
 * methods and fields which the user intends to add for all join points and are not intended to be used in LARA aspects.
 * The implementation of the abstract methods is mandatory!
 * 
 * @author Lara C.
 */
public abstract class AMWeaver extends LaraWeaverEngine {

    /**
     * Get the list of available actions in the weaver
     * 
     * @return list with all actions
     */
    @Override
    public final List<String> getActions() {
        String[] weaverActions = { "insertBefore", "insertBefore", "insertAfter", "insertAfter", "detach",
                "setGlobalType", "addFile", "interchange", "defType", "appendInput", "appendOutput", "prependInput",
                "prependOutput", "addGlobal", "insertReturn", "insertBegin", "insertBegin", "insertEnd", "insertEnd",
                "appendOutput", "prependOutput", "appendArgument", "prependArgument" };
        return Arrays.asList(weaverActions);
    }

    /**
     * Returns the name of the root
     * 
     * @return the root name
     */
    @Override
    public final String getRoot() {
        return "app";
    }

    /**
     * Returns a list of classes that may be imported and used in LARA.
     * 
     * @return a list of importable classes
     */
    @Override
    public final List<Class<?>> getAllImportableClasses() {
        Class<?>[] defaultClasses = { Sym.class };
        List<Class<?>> otherClasses = this.getImportableClasses();
        List<Class<?>> allClasses = new ArrayList<>(Arrays.asList(defaultClasses));
        allClasses.addAll(otherClasses);
        return allClasses;
    }

    /**
     * Does the generated code implements events?
     * 
     * @return true if implements events, false otherwise
     */
    @Override
    public final boolean implementsEvents() {
        return true;
    }
}
