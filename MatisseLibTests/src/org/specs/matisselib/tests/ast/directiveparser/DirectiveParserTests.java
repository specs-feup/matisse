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

package org.specs.matisselib.tests.ast.directiveparser;

import org.junit.Assert;
import org.junit.Test;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.TokenReportingService;
import org.specs.matisselib.ssa.BlockContext;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.tests.TestUtils;
import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.reporting.MessageType;

public class DirectiveParserTests {

    @Test
    public void testSimple() {
        FunctionBody body = new FunctionBody();

        SsaBlock block = new SsaBlock();
        body.addBlock(block);

        TokenReportingService reportService = new TokenReportingService() {

            @Override
            public void emitMessage(MatlabNode source, MessageType type, String message) {
                throw new RuntimeException("Message: " + message);
            }
        };
        DataStore dataStore = new SimpleDataStore("test");
        dataStore.add(PassManager.NODE_REPORTING, reportService);

        new DirectiveParser().parseDirective(StatementFactory.newComment(1, "!specialize A"),
                new BlockContext(body, null, block, 0),
                dataStore);

        String obtained = body.toString();

        Assert.assertEquals(
                TestUtils.normalize(SpecsIo.getResource(DirectiveParserResource.SIMPLE)),
                TestUtils.normalize(obtained));
    }
}
