/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.io.File;
import java.lang.reflect.Method;

import static com.badlogic.gdx.utils.SharedLibraryLoader.*;
import static com.badlogic.gdx.utils.SharedLibraryLoader.is64Bit;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isMac;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isWindows;

/** Loads shared libraries from JAR files. Call {@link JglfwNativesLoader#load() to load the
 * required LWJGL 3 native shared libraries.
 * @author mzechner
 * @author Nathan Sweet */
public class JglfwNativesLoader {
	static boolean load = true;

	static {
		// Don't extract natives if using JWS.
		try {
			Method method = Class.forName("javax.jnlp.ServiceManager").getDeclaredMethod("lookup", new Class[]{String.class});
			method.invoke(null, "javax.jnlp.PersistenceService");
			load = false;
		} catch (Throwable ex) {
			load = true;
		}
	}

	/** Extracts the LWJGL native libraries from the classpath and sets the "org.lwjgl.librarypath" system property. */
	static public synchronized void load() {
		load(false);
	}

	/** Extracts the LWJGL native libraries from the classpath and sets the "org.lwjgl.librarypath" system property. */
	static public synchronized void load(boolean disableOpenAL) {
		if (!load) return;

		SharedLibraryLoader loader = new SharedLibraryLoader();
		File nativesDir = null;
		try {
			if (isWindows) {
				nativesDir = loader.extractFile(is64Bit ? "lwjgl.dll" : "lwjgl32.dll", null).getParentFile();
				if (!disableOpenAL)
					loader.extractFile(is64Bit ? "OpenAL.dll" : "OpenAL32.dll", nativesDir.getName());
			} else if (isMac) {
				nativesDir = loader.extractFile("liblwjgl.dylib", null).getParentFile();
				if (!disableOpenAL) loader.extractFile("libopenal.dylib", nativesDir.getName());
			} else if (isLinux) {
				nativesDir = loader.extractFile(is64Bit ? "liblwjgl.so" : "liblwjgl32.so", null).getParentFile();
				if (!disableOpenAL)
					loader.extractFile(is64Bit ? "libopenal.so" : "libopenal32.so", nativesDir.getName());
			}
		} catch (Throwable ex) {
			throw new RuntimeException("Unable to extract LWJGL natives.", ex);
		}
		System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
		load = false;
	}
}