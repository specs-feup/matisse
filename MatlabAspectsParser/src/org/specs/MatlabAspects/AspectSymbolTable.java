package org.specs.MatlabAspects;
//
//  SymbolTable.java
//  
//
//  Created by Luís Cruz on 8/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.HashMap;


public class AspectSymbolTable {
	public AspectSymbolTable parent = null; //so sera necessario de forem implementados scopes aninhados
	public HashMap<String, AspectSymbol> symbols;
	
	public AspectSymbolTable(){
		symbols = new HashMap<>();
	}
	
	public AspectSymbolTable(AspectSymbolTable parent){
		this.parent = parent;
		symbols = new HashMap<>();
	}
	
	public void add(AspectSymbol s){
		symbols.put(s.name, s);
	}
	
	public AspectSymbol get(String symbolName){
		return symbols.get(symbolName);
	}
	
	public AspectSymbol lookup(String symbolName){
		AspectSymbol s = get(symbolName);
		if(s!=null){
			return s;
		}
		else if (parent != null){
			return parent.lookup(symbolName);
		}
		return null;
	}
	
	public void print(){
		for(AspectSymbol s: symbols.values()){
			System.out.println(s);
		}
	}
}
