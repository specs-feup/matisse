package org.specs.matisselib.services.log;

import org.specs.matisselib.services.Logger;

public class NullLogger implements Logger {

	@Override
	public void log(Object msg) {
		// Do nothing
	}

}
