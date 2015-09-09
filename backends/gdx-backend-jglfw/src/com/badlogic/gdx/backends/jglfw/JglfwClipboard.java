/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.utils.Clipboard;

import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;

/** Clipboard implementation for desktop that uses the system clipboard via GLFW
 * @author mzechner */
public class JglfwClipboard implements Clipboard {
	private final JglfwApplication app;

	public JglfwClipboard(JglfwApplication app) {
		this.app = app;
	}

	public String getContents () {
		return glfwGetClipboardString(app.graphics.window);
	}

	public void setContents (String content) {
		glfwSetClipboardString(app.graphics.window, content);
	}
}
