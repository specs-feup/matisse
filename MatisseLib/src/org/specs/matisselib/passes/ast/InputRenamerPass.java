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

package org.specs.matisselib.passes.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.AMatlabNodePass;
import org.specs.matisselib.PassMessage;
import org.specs.matisselib.PreTypeInferenceServices;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.NamingService;
import org.specs.matisselib.services.TokenReportingService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public class InputRenamerPass extends AMatlabNodePass {

    private final DataKey<TokenReportingService> reportKey;
    private final DataKey<NamingService> namingKey;

    private InputRenamerPass(DataKey<TokenReportingService> reportKey, DataKey<NamingService> namingKey) {
	this.reportKey = reportKey;
	this.namingKey = namingKey;
    }

    public InputRenamerPass() {
	this(PassManager.NODE_REPORTING, PreTypeInferenceServices.COMMON_NAMING);
    }

    public static InputRenamerPass newInstanceV2() {
	// Original key has more methods than TokenReportService, adapting key
	DataKey<TokenReportingService> reportKey = KeyFactory.object(PassManager.NODE_REPORTING.getName(),
		TokenReportingService.class);

	return new InputRenamerPass(reportKey, PreTypeInferenceServices.COMMON_NAMING);
    }

    @Override
    public Collection<DataKey<?>> getReadKeys() {
	return Arrays.asList(reportKey, namingKey);
    }

    @Override
    public MatlabNode apply(MatlabNode rootNode, DataStore data) {
	Preconditions.checkArgument(rootNode != null);
	Preconditions.checkArgument(data != null);

	NamingService namingService = data.get(namingKey);
	TokenReportingService reportingService = data.get(reportKey);

	FunctionNode functionNode = (FunctionNode) rootNode;
	FunctionDeclarationSt functionDecl = functionNode.getDeclarationNode();

	int previousStatementIndex = functionNode.indexOfChild(functionDecl) + 1;

	List<String> outputNames = functionDecl.getOutputs().getNames();
	List<String> previousInputs = new ArrayList<>();
	MatlabNodeIterator inputs = functionDecl.getInputs().getChildrenIterator();

	while (inputs.hasNext()) {
	    Optional<IdentifierNode> potentialIdentifier = inputs.next(IdentifierNode.class);
	    if (potentialIdentifier.isPresent()) {
		IdentifierNode identifier = potentialIdentifier.get();

		String identifierName = identifier.getName();
		if (previousInputs.contains(identifierName)) {
		    throw reportingService.emitError(identifier, PassMessage.PARSE_ERROR,
			    "Multiple function inputs with the same name");
		}
		previousInputs.add(identifierName);

		if (outputNames.contains(identifierName)) {
		    // Apply replacement

		    String newName = namingService.generateTemporaryVariableName(identifierName);

		    IdentifierNode newInput = MatlabNodeFactory.newIdentifier(newName);
		    inputs.set(newInput);

		    StatementData stData = new StatementData(functionDecl.getLine(), false);
		    MatlabNode assignment = StatementFactory.newAssignment(stData, identifier, newInput);

		    functionNode.addChild(previousStatementIndex, assignment);

		    previousStatementIndex++;
		}
	    }
	}

	return rootNode;
    }
}
