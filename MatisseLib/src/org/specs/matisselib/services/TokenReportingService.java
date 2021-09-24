/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matisselib.services;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.matisselib.passmanager.PassManager;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;
import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Reports errors or warnings.
 * <p>
 * This is similar to {@link Reporter}, however the node indicating the source of the problem is required.
 * 
 * @author Luís Reis
 * @see PassManager#NODE_REPORTING
 *
 */
public interface TokenReportingService {
    void emitMessage(MatlabNode source, MessageType type, String message);

    default RuntimeException emitError(MatlabNode source, MessageType type, String message) {
        Preconditions.checkArgument(type.getMessageCategory() == ReportCategory.ERROR);

        emitMessage(source, type, message);

        return new RuntimeException(message);
    }

}
