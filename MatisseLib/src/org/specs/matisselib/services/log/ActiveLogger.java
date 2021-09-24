package org.specs.matisselib.services.log;

import org.specs.matisselib.services.Logger;

public class ActiveLogger implements Logger {
	private final String passName;
	
	public ActiveLogger(String passName) {
		this.passName = passName;
	}
	
	@Override
	public void log(Object msg) {
		System.out.print("[");
		System.out.print(passName);
		System.out.print("] ");
		System.out.println(msg);
	}
}
