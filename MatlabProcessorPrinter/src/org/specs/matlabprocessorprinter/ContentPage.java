/**
 * Copyright 2016 SPeCS.
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

package org.specs.matlabprocessorprinter;

public class ContentPage {
	private final String title;
	private final String code;
	private final String contentType;

	public ContentPage(String title, String code, String contentType) {
		this.title = title;
		this.code = code;
		this.contentType = contentType;
	}

	public String getTitle() {
		return title;
	}

	public String getCode() {
		return code;
	}

	public String getContentType() {
		return contentType;
	}
}
