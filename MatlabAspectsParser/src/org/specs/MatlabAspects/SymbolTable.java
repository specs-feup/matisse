package org.specs.MatlabAspects;

//
// SymbolTable.java
//
//
// Created by Luís Cruz on 8/13/09.
// Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Joao Bispo
 */
public class SymbolTable {
    // public SymbolTable parent = null; // so sera necessario de forem
    // implementados scopes aninhados
    // public HashMap<String, Symbol> symbols;
    public Map<String, Symbol> symbols;

    public SymbolTable() {
	symbols = new HashMap<>();
    }

    public SymbolTable(SymbolTable parent) {
	// this.parent = parent;
	symbols = new HashMap<>();
    }

    public void add(Symbol s) {
	symbols.put(s.name, s);
    }

    public Symbol get(String symbolName) {
	return symbols.get(symbolName);
    }

    public Symbol lookup(String symbolName) {
	Symbol s = get(symbolName);
	if (s != null) {
	    return s;
	}
	/*
	} else if (parent != null) {
	    return parent.lookup(symbolName);
	}
	*/
	return null;
    }

    public void print() {
	for (Symbol s : symbols.values()) {
	    System.out.println(s);
	}
    }

    public Set<String> keySet() {
	return symbols.keySet();
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
	// TODO Auto-generated method stub
	return symbols.toString();
    }
}
