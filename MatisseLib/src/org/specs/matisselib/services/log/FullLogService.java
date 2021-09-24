package org.specs.matisselib.services.log;

import org.specs.matisselib.services.LogService;
import org.specs.matisselib.services.Logger;

public class FullLogService implements LogService {

	@Override
	public Logger getLogger(String passName) {
		return new ActiveLogger(passName);
	}

}
