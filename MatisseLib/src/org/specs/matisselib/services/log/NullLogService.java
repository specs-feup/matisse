package org.specs.matisselib.services.log;

import org.specs.matisselib.services.LogService;
import org.specs.matisselib.services.Logger;

public class NullLogService implements LogService{

	@Override
	public Logger getLogger(String passIdentification) {
		return new NullLogger();
	}

}
