/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabProcessor.commands;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum CommandsTestResource implements ResourceProvider {

    // Pedantic refers to unusual cases that are probably not used very often.
    // It would be nice to support them, but it might not be worth the effort.

    // PEDANTIC_COMMAND1,
    // PEDANTIC_COMMAND2,
    COMMAND1,
    COMMAND2,
    COMMAND3,
    COMMAND4,
    COMMAND5,
    COMMAND6,
    COMMAND7,
    COMMAND8,
    // COMMAND9,
    COMMAND10,
    COMMAND11,
    COMMAND12,
    COMMAND13,
    COMMAND14,
    COMMAND15,
    COMMAND16,
    TEXT;

    private static final String LOCATION = "commands/";
    private final String name;

    private CommandsTestResource() {
        this.name = name().toLowerCase();
    }

    @Override
    public String getResource() {
        return LOCATION + name + ".m";
    }

    public String getResultResource() {
        return LOCATION + name + ".txt";
    }
}
