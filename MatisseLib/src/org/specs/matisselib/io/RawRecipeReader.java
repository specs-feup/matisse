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

package org.specs.matisselib.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.suikasoft.jOptions.DataStore.SimpleDataStore;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

public class RawRecipeReader<PassType> implements AutoCloseable {
    private final BufferedReader reader;
    private final String versionName;
    private final Function<String, Class<? extends PassType>> passGetter;

    public RawRecipeReader(Reader reader, String versionName, Function<String, Class<? extends PassType>> passGetter) {
        Preconditions.checkArgument(reader != null);
        Preconditions.checkArgument(versionName != null);
        Preconditions.checkArgument(passGetter != null);

        this.reader = new BufferedReader(reader);
        this.versionName = versionName;
        this.passGetter = passGetter;
    }

    public RawRecipeReader(InputStream inputStream, String versionName,
            Function<String, Class<? extends PassType>> passGetter) {
        this(new InputStreamReader(inputStream), versionName, passGetter);
    }

    @SuppressWarnings("unchecked")
    public PassType readPass() throws IOException {
        String line;

        for (;;) {
            line = this.reader.readLine();
            if (line == null) {
                return null;
            }

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("#")) {
                // Comment
                continue;
            }

            if (line.startsWith("!")) {
                if (line.equals("!" + this.versionName)) {
                    // OK
                    continue;
                }
                throw new RuntimeException("Unrecognized recipe file format: " + line);
            }

            int sep = line.indexOf(':');
            if (sep == -1) {
                sep = line.length();
            }

            String passName = line.substring(0, sep).trim();
            String parameters = line.substring(sep).trim();

            Class<? extends PassType> clazz = this.passGetter.apply(passName);
            if (clazz == null) {
                throw new RuntimeException("Pass not found: '" + passName + "'");
            }

            Map<String, DataKey<?>> availableParameters = new HashMap<>();
            try {
                try {
                    Method method = clazz
                            .getMethod("getRequiredParameters");
                    if ((method.getModifiers() & Modifier.STATIC) == 0) {
                        throw new RuntimeException(passName + ".getRequiredParameters should be static");
                    }

                    for (DataKey<?> parameter : (List<DataKey<?>>) method
                            .invoke(null)) {

                        availableParameters.put(parameter.getName(), parameter);
                    }
                } catch (NoSuchMethodException e) {
                    // Ignore
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | SecurityException e) {
                throw new RuntimeException(e);
            }

            DataStore dataStore = new SimpleDataStore("loaded-store");

            while (!parameters.isEmpty()) {
                // Remove leading ':' or ',' character.
                parameters = parameters.substring(1);

                int eqSep = parameters.indexOf('=');
                if (eqSep == -1) {
                    throw new RuntimeException("Incorrect format at " + line + ", got \"" + parameters + "\"");
                }

                String parameterName = parameters.substring(0, eqSep).trim();
                String remainder = parameters.substring(eqSep + 1).trim();

                Object value;
                if (remainder.startsWith("\"")) {
                    StringBuilder builder = new StringBuilder();
                    int index = 1;
                    for (;;) {
                        if (index >= remainder.length()) {
                            throw new RuntimeException("String was not terminated");
                        }
                        char ch = remainder.charAt(index++);
                        if (ch == '"') {
                            break;
                        }
                        if (ch == '\\') {
                            if (index >= remainder.length()) {
                                throw new RuntimeException("String was not terminated");
                            }
                            ch = remainder.charAt(index++);
                            if (ch == 'r') {
                                builder.append("\r");
                            } else if (ch == 'n') {
                                builder.append("\n");
                            } else if (ch == '"') {
                                builder.append("\"");
                            } else if (ch == 'u') {
                                if (index + 3 >= remainder.length()) {
                                    throw new RuntimeException(
                                            "Expected 4 hexadecimal digits after unicode escape sequence");
                                }
                                String unicode = remainder.substring(index, index + 4);
                                int unicodeValue = Integer.parseInt(unicode, 16);

                                builder.append((char) unicodeValue);

                                index += 4;
                            } else {
                                throw new RuntimeException("Unrecognized escape literal");
                            }
                        }
                        builder.append(ch);
                    }
                    value = builder.toString();

                    parameters = remainder.substring(index).trim();
                } else {

                    int nextSep = remainder.indexOf(',');

                    String valueStr;
                    if (nextSep == -1) {
                        valueStr = remainder;
                        parameters = "";
                    } else {
                        valueStr = remainder.substring(0, nextSep).trim();
                        parameters = remainder.substring(nextSep).trim();
                    }

                    if (valueStr.equals("<null>")) {
                        value = null;
                    } else if (valueStr.matches("^-?[0-9]+$")) {
                        value = Integer.parseInt(valueStr, 10);
                    } else if (valueStr.equals("true")) {
                        value = true;
                    } else if (valueStr.equals("false")) {
                        value = false;
                    } else if (valueStr.equals("<empty>")) {
                        value = Optional.empty();
                    } else {
                        throw new RuntimeException("Unrecognized value: '" + valueStr + "'");
                    }
                }

                DataKey<?> key = availableParameters.get(parameterName);
                if (key == null) {
                    throw new RuntimeException(
                            "For pass " + passName + ": Unrecognized parameter '" + parameterName + "', options are "
                                    + availableParameters);
                }

                if (key.getValueClass() == Class.class) {
                    try {
                        value = Class.forName(value.toString());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Could not find class " + value);
                    }
                }

                dataStore.add((DataKey<Object>) key, value);
            }

            DataView dataView = DataView.newInstance(dataStore);

            try {
                try {
                    Constructor<? extends PassType> ctor = clazz.getConstructor(DataView.class);
                    return ctor.newInstance(dataView);
                } catch (NoSuchMethodException e) {
                    Constructor<? extends PassType> ctor = clazz.getConstructor();
                    return ctor.newInstance();
                }
            } catch (NoSuchMethodException
                    | SecurityException
                    | InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}
