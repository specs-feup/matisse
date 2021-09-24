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

package org.specs.CIR.Utilities;

import java.util.Collection;
import java.util.List;

import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;

public class DecoderDefault implements TypeDecoder {

    private final List<TypeDecoder> decoders;
    private List<String> supportedTypes;

    public DecoderDefault() {
        this.decoders = SpecsFactory.newArrayList();
        this.supportedTypes = null;
    }

    @Override
    public VariableType decode(String typeString) {

        for (TypeDecoder decoder : decoders) {
            VariableType type = decoder.decode(typeString);
            if (type != null) {
                return type;
            }
        }

        SpecsLogs.warn(
                "Could not decode variable type from string '" + typeString + "'.\nSupported types: "
                        + supportedTypes());
        return null;
    }

    public void addDecoder(TypeDecoder decoder) {
        decoders.add(decoder);
    }

    @Override
    public Collection<String> supportedTypes() {
        if (supportedTypes == null) {
            supportedTypes = initSupportedTypes();
        }

        return supportedTypes;
    }

    private List<String> initSupportedTypes() {
        List<String> supportedTypes = SpecsFactory.newArrayList();

        for (TypeDecoder decoder : decoders) {
            supportedTypes.addAll(decoder.supportedTypes());
        }

        return supportedTypes;
    }

    @Override
    public String toString() {
        return "[DecoderDefault " + decoders + "]";
    }

}
