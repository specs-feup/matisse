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

package org.specs.matisselib;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;

public enum PassMessage implements MessageType {
    /**
     * Errors that are triggered at compile-time even in MATLAB
     */
    PARSE_ERROR(ReportCategory.ERROR, "Parse Error"),
    /**
     * Errors caused by limitations in the type inference system.
     */
    TYPE_INFERENCE_FAILURE(ReportCategory.ERROR, "Type Inference Failure"),
    /**
     * Errors caused by code that is certainly incorrect (i.e. causes a runtime error in MATLAB).<br/>
     * For MATISSE extensions, these refer to code that violates the MATISSE requirements.
     */
    CORRECTNESS_ERROR(ReportCategory.ERROR, "Incorrect Code"),
    /**
     * Indicates that a variable or function was not found. Can be a case of CORRECTNESS_ERROR, but it can also be the
     * result of a missing function in MATISSE.
     */
    MISSING_IDENTIFIER(ReportCategory.ERROR, "Missing Variable or Function"),
    /**
     * Code that is likely to be wrong.
     */
    SUSPICIOUS_CASE(ReportCategory.WARNING, "Suspicious Code"),
    /**
     * Missing features
     */
    NOT_YET_IMPLEMENTED(ReportCategory.ERROR, "Not yet implemented"),
    /**
     * Similar to {@link #NOT_YET_IMPLEMENTED}, but we have no plans to support any time soon.
     */
    NOT_SUPPORTED(ReportCategory.ERROR, "Not supported"),
    /**
     * Internal MATISSE error.
     */
    INTERNAL_ERROR(ReportCategory.ERROR, "Internal error"),
    /**
     * Error caused by incorrect aspect-file information
     */
    ASPECT_ERROR(ReportCategory.ERROR, "Aspect file error"),

    FUNCTION_NOT_FOUND(ReportCategory.ERROR, "Function not found"),

    SPECIALIZATION_FAILURE(ReportCategory.ERROR, "Function specialization failure"),

    OPTIMIZATION_OPPORTUNITY(ReportCategory.WARNING, "Optimization Opportunity"),

    RECOVERABLE_PROBLEM(ReportCategory.WARNING, "Recoverable Problem"),

    UNRECOGNIZED_DIRECTIVE(ReportCategory.WARNING, "Unrecognized Directive"),

    /**
     * When a directive is valid in MATISSE (e.g. %!parallel) but the specific parameters are not.
     */
    INVALID_DIRECTIVE_FORMAT(ReportCategory.ERROR, "Invalid Directive Format");

    private final ReportCategory reportCategory;
    private final String message;

    private PassMessage(ReportCategory reportCategory, String message) {
        this.reportCategory = reportCategory;
        this.message = message;
    }

    @Override
    public ReportCategory getMessageCategory() {
        return this.reportCategory;
    }

    @Override
    public String toString() {
        return this.message;
    }

}
