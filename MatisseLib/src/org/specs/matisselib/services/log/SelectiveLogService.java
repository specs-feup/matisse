package org.specs.matisselib.services.log;

import java.util.List;

import org.specs.matisselib.services.LogService;
import org.specs.matisselib.services.Logger;

public class SelectiveLogService implements LogService {

    private final List<String> passesToLog;

    public SelectiveLogService(List<String> passesToLog) {
        this.passesToLog = passesToLog;
    }

    @Override
    public Logger getLogger(String passIdentification) {
        if (!passesToLog.contains(passIdentification)) {
            return new NullLogger();
        }

        return new ActiveLogger(passIdentification);
    }

}
