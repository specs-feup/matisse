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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class RawRecipeWriter implements AutoCloseable {
    private final Writer writer;

    public RawRecipeWriter(Writer writer) {
        this.writer = writer;
    }

    public RawRecipeWriter(OutputStream outputStream) {
        this(new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
    }

    public void writeComment(String comment) throws IOException {
        Preconditions.checkArgument(comment != null);
        Preconditions.checkArgument(!comment.contains("\r"));
        Preconditions.checkArgument(!comment.contains("\n"));

        writer.write("# ");
        writer.write(comment);
        writer.write("\r\n");
    }

    public void writeEmptyLine() throws IOException {
        writer.write("\r\n");
    }

    public void writeVersionName(String versionName) throws IOException {
        writer.write("!");
        writer.write(versionName);
        writer.write("\r\n");
    }

    public void writePass(String name, DataView parameters) throws IOException {
        Preconditions.checkArgument(name != null);
        Preconditions.checkArgument(parameters != null);

        writer.write(name);

        // Map<String, Object> values = parameters.getValuesMap();
        Collection<String> keys = parameters.getKeysWithValues();
        // Collection<DataKey<?>> keys = parameters.getKeys();
        // if (!values.isEmpty()) {
        if (!keys.isEmpty()) {
            writer.write(": ");

            boolean isFirst = true;
            // for (String key : values.keySet()) {
            for (String key : keys) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    writer.write(", ");
                }
                writer.write(key);

                writer.write("=");

                // Object value = values.get(key);
                Object value = parameters.getValueRaw(key);
                writeValue(writer, value);
            }
        }

        writer.write("\r\n");
    }

    private static void writeValue(Writer writer, Object value) throws IOException {
        if (value == null) {
            writer.write("<null>");
        } else if (value instanceof Boolean || value instanceof Integer) {
            writer.write(value.toString());
        } else if (value instanceof Optional<?>) {
            Optional<?> opt = (Optional<?>) value;
            if (opt.isPresent()) {
                writeValue(writer, opt.get());
            } else {
                writer.write("<empty>");
            }
        } else if (value instanceof String) {
            String str = (String) value;

            writer.write("\"");
            for (int i = 0; i < str.length(); ++i) {
                char ch = str.charAt(i);

                if (ch >= 32 && ch <= 126 &&
                        ch != '"' && ch != '\\') {

                    writer.write(ch);
                } else if (ch == '\r') {
                    writer.write("\\r");
                } else if (ch == '\n') {
                    writer.write("\\n");
                } else if (ch == '"') {
                    writer.write("\\\"");
                } else {
                    writer.write("\\u");
                    writer.write(String.format(Locale.UK, "%04X", (int) ch));
                }
            }
            writer.write("\"");
        } else if (value instanceof Class) {
            writeValue(writer, ((Class<?>) value).getName());
        } else {
            throw new NotImplementedException(value);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
