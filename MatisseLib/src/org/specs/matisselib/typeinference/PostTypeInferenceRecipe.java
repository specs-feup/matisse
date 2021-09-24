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

package org.specs.matisselib.typeinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

public class PostTypeInferenceRecipe {
    private final List<PostTypeInferencePass> passes;

    public PostTypeInferenceRecipe(List<PostTypeInferencePass> passes) {
        Preconditions.checkArgument(passes != null);

        this.passes = new ArrayList<>(passes);
    }

    public List<PostTypeInferencePass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Stream<PostTypeInferencePass> getPassesStream() {
        return passes.stream();
    }

    public PostTypeInferencePass get(int index) {
        return passes.get(index);
    }

    public int size() {
        return passes.size();
    }

    public static PostTypeInferenceRecipe empty() {
        return new PostTypeInferenceRecipe(Collections.emptyList());
    }

    public static PostTypeInferenceRecipe fromSinglePass(PostTypeInferencePass pass) {
        Preconditions.checkArgument(pass != null);

        return new PostTypeInferenceRecipe(Arrays.asList(pass));
    }

    public static PostTypeInferenceRecipe combine(PostTypeInferenceRecipe... recipes) {
        PostTypeInferenceRecipeBuilder builder = new PostTypeInferenceRecipeBuilder();

        Stream.of(recipes)
                .flatMap(PostTypeInferenceRecipe::getPassesStream)
                .forEach(builder::addPass);

        return builder.getRecipe();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("PostTypeInferenceRecipe[\n");

        for (PostTypeInferencePass pass : passes) {
            result.append('\t');
            result.append(pass.getName());
            result.append('\n');
        }

        result.append("]");
        return result.toString();
    }
}
