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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.specs.MatlabIR.MatlabLanguage.ReservedWord;

import com.google.common.base.Preconditions;

public abstract class NamingService {

    private static final Set<String> BLACK_LIST;
    static {
	BLACK_LIST = new HashSet<>();

	for (ReservedWord reservedWord : ReservedWord.values()) {
	    BLACK_LIST.add(reservedWord.getLiteral());
	}

	BLACK_LIST.add("ans");
	BLACK_LIST.add("false");
	BLACK_LIST.add("i");
	BLACK_LIST.add("pi");
	BLACK_LIST.add("true");

	Preconditions.checkArgument(BLACK_LIST.stream().allMatch(n -> n != null));
    }

    public static Set<String> getBlackList() {
	return Collections.unmodifiableSet(BLACK_LIST);
    }

    public String generateTemporaryVariableName(String suggestion) {
	Preconditions.checkArgument(suggestion != null);
	Preconditions.checkArgument(!suggestion.isEmpty());

	// First, we sanitize the suggestion
	suggestion = suggestion.replaceAll("[^a-zA-Z0-9]", "_")
		.replaceAll("_+", "_");
	if (suggestion.charAt(0) == '_') {
	    suggestion = "v" + suggestion;
	}

	if (suggestion.charAt(suggestion.length() - 1) != '_') {
	    suggestion += "_";
	}

	for (int suffix = 1;; ++suffix) {
	    String proposal = suggestion + suffix;
	    if (!isVariableNameReserved(proposal)) {
		return proposal;
	    }
	}
    }

    public abstract boolean isVariableNameReserved(String suggestion);
}
