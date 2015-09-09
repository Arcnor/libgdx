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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/** An implementation of the {@link Graphics} interface based on GLFW.
 * @author Nathan Sweet */
public class JglfwGraphics implements Graphics {
	static final boolean isMac = System.getProperty("os.name").contains("OS X");
	static final boolean isWindows = System.getProperty("os.name").contains("Windows");
	static final boolean isLinux = System.getProperty("os.name").contains("Linux");

	static int glMajorVersion, glMinorVersion;

	long window;
	private boolean fullscreen;
	private long fullscreenMonitor;
	private String title;
	private boolean resizable, undecorated;
	private BufferFormat bufferFormat;
	private boolean vSync;
	private int x, y, width, height;
	private boolean visible;
	private Color initialBackgroundColor;
	private volatile boolean isContinuous = true, renderRequested;
	volatile boolean foreground, minimized;
	private IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
	private IntBuffer intBuffer2 = BufferUtils.createIntBuffer(1);

	private long frameId = -1;
	private float deltaTime;
	private long frameStart, lastTime = -1;
	private int frames, fps;

	boolean usingGL30;

	private JglfwGL20 gl20;
	private JglfwGL30 gl30;

	public JglfwGraphics (JglfwApplicationConfiguration config) {
		// Store values from config.
		bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples, false);
		title = config.title;
		resizable = config.resizable;
		undecorated = config.undecorated;
		x = config.x;
		y = config.y;
		vSync = config.vSync;

		// FIXME: This needs smarts
		usingGL30 = config.useGL30;

		initialBackgroundColor = config.initialBackgroundColor;
		if (config.fullscreenMonitorIndex != -1) { // Use monitor specified in config if it is valid.
			PointerBuffer monitors = glfwGetMonitors();
			long count = monitors.get();
			if (config.fullscreenMonitorIndex < count) fullscreenMonitor = monitors.get(config.fullscreenMonitorIndex + 1);
		}

		// Create window.
		if (!createWindow(config.width, config.height, config.fullscreen)) {
			throw new GdxRuntimeException("Unable to create window: " + config.width + "x" + config.height + ", fullscreen: "
					+ config.fullscreen);
		}

		GLContext.createFromCurrent();

		// Create GL.
		String version = GL11.glGetString(GL20.GL_VERSION);
		glMajorVersion = Integer.parseInt("" + version.charAt(0));
		glMinorVersion = Integer.parseInt("" + version.charAt(2));

		if (glMajorVersion <= 1)
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version);
		if (glMajorVersion == 2 || version.contains("2.1")) {
			if (!supportsExtension("GL_EXT_framebuffer_object") && !supportsExtension("GL_ARB_framebuffer_object")) {
				throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version
						+ ", FBO extension: false");
			}
		}

		if (usingGL30) {
			gl30 = new JglfwGL30();
			gl20 = gl30;
		} else {
			gl20 = new JglfwGL20();
		}

		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		Gdx.gl30 = gl30;

		if (!config.hidden) show();
	}

	private boolean createWindow (int width, int height, boolean fullscreen) {
		if (fullscreen && fullscreenMonitor == 0) fullscreenMonitor = getWindowMonitor();

		glfwWindowHint(GLFW_VISIBLE, 0);
		glfwWindowHint(GLFW_RESIZABLE, resizable ? 1 : 0);
		glfwWindowHint(GLFW_DECORATED, undecorated ? 0 : 1);
		glfwWindowHint(GLFW_RED_BITS, bufferFormat.r);
		glfwWindowHint(GLFW_GREEN_BITS, bufferFormat.g);
		glfwWindowHint(GLFW_BLUE_BITS, bufferFormat.b);
		glfwWindowHint(GLFW_ALPHA_BITS, bufferFormat.a);
		glfwWindowHint(GLFW_DEPTH_BITS, bufferFormat.depth);
		glfwWindowHint(GLFW_STENCIL_BITS, bufferFormat.stencil);
		glfwWindowHint(GLFW_SAMPLES, bufferFormat.samples);

		boolean mouseCaptured = window != 0 && glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;

		long oldWindow = window;
		long newWindow = glfwCreateWindow(width, height, title, fullscreen ? fullscreenMonitor : 0, oldWindow);
		if (newWindow == 0) return false;
		if (oldWindow != 0) glfwDestroyWindow(oldWindow);
		window = newWindow;
		this.width = Math.max(1, width);
		this.height = Math.max(1, height);

		this.fullscreen = fullscreen;
		if (!fullscreen) {
			if (x == -1 || y == -1) {
				DisplayMode mode = getDesktopDisplayMode();
				x = (mode.width - width) / 2;
				y = (mode.height - height) / 2;
			}
			glfwSetWindowPos(window, x, y);
		}

		if (!mouseCaptured) glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL); // Prevent fullscreen from taking mouse.

		glfwMakeContextCurrent(newWindow);
		setVSync(vSync);
		if (visible) glfwShowWindow(window);

		return true;
	}

	void frameStart (long time) {
		if (lastTime == -1) lastTime = time;
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
		frameId++;
	}

	void sizeChanged (int width, int height) {
		if (isMac) {
			glfwShowWindow(window); // This is required to refresh the NSOpenGLContext on OSX!
		}
		width = Math.max(1, width);
		height = Math.max(1, height);
		this.width = width;
		this.height = height;
		Gdx.gl.glViewport(0, 0, width, height);
		ApplicationListener listener = Gdx.app.getApplicationListener();
		if (listener != null) listener.resize(width, height);
		requestRendering();
	}

	void positionChanged (int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public long getFrameId () {
		return frameId;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public float getRawDeltaTime () {
		return deltaTime;
	}

	public int getFramesPerSecond () {
		return fps;
	}

	public GraphicsType getType () {
		return GraphicsType.JGLFW;
	}

	public float getPpiX () {
		intBuffer.clear();

		glfwGetMonitorPhysicalSize(getWindowMonitor(), intBuffer, null);
		final ByteBuffer vidMode = glfwGetVideoMode(getWindowMonitor());
		int pW = intBuffer.get();
		return GLFWvidmode.width(vidMode) / (pW * 0.03937f);
	}

	public float getPpiY () {
		intBuffer.clear();

		glfwGetMonitorPhysicalSize(getWindowMonitor(), null, intBuffer);
		final ByteBuffer vidMode = glfwGetVideoMode(getWindowMonitor());
		int pH = intBuffer.get();
		return GLFWvidmode.height(vidMode) / (pH * 0.03937f);
	}

	public float getPpcX () {
		intBuffer.clear();

		glfwGetMonitorPhysicalSize(getWindowMonitor(), intBuffer, null);
		final ByteBuffer vidMode = glfwGetVideoMode(getWindowMonitor());
		int pW = intBuffer.get();
		return GLFWvidmode.width(vidMode) / (pW / 10);
	}

	public float getPpcY () {
		intBuffer.clear();

		glfwGetMonitorPhysicalSize(getWindowMonitor(), null, intBuffer);
		final ByteBuffer vidMode = glfwGetVideoMode(getWindowMonitor());
		int pH = intBuffer.get();
		return GLFWvidmode.height(vidMode) / (pH / 10);
	}

	public float getDensity () {
		intBuffer.clear();
		intBuffer2.clear();

		glfwGetMonitorPhysicalSize(getWindowMonitor(), intBuffer, intBuffer2);
		float mmWidth = intBuffer.get();
		float mmHeight = intBuffer2.get();
		float inches = (float) Math.sqrt(mmWidth * mmWidth + mmHeight * mmHeight) * 0.03937f; // mm to inches
		final ByteBuffer vidMode = glfwGetVideoMode(getWindowMonitor());
		float pixelWidth = GLFWvidmode.width(vidMode);
		float pixelHeight = GLFWvidmode.height(vidMode);
		float pixels = (float) Math.sqrt(pixelWidth * pixelWidth + pixelHeight * pixelHeight);
		float diagonalPpi = pixels / inches;

		return diagonalPpi / 160f;
	}

	public boolean supportsDisplayModeChange () {
		return true;
	}

	private long getWindowMonitor () {
		if (window != 0) {
			long monitor = glfwGetWindowMonitor(window);
			if (monitor != 0) return monitor;
		}
		return glfwGetPrimaryMonitor();
	}

	public DisplayMode[] getDisplayModes () {
		Array<DisplayMode> modes = new Array<DisplayMode>();
		IntBuffer count = BufferUtils.createIntBuffer(1);
		ByteBuffer vidModes = glfwGetVideoModes(getWindowMonitor(), count);
		int vidModeCount = count.get();
		for (int j = 0; j < vidModeCount; j++) {

			modes.add(new JglfwDisplayMode(
					GLFWvidmode.width(vidModes),
					GLFWvidmode.height(vidModes),
					GLFWvidmode.refreshRate(vidModes),
					GLFWvidmode.redBits(vidModes) + GLFWvidmode.greenBits(vidModes) + GLFWvidmode.blueBits(vidModes)
			));
			vidModes.position(GLFWvidmode.SIZEOF * (j + 1));
		}
		return modes.toArray(DisplayMode.class);
	}

	public DisplayMode getDesktopDisplayMode () {
		ByteBuffer mode = glfwGetVideoMode(getWindowMonitor());
		return new JglfwDisplayMode(GLFWvidmode.width(mode), GLFWvidmode.height(mode), GLFWvidmode.refreshRate(mode), GLFWvidmode.redBits(mode) + GLFWvidmode.greenBits(mode) + GLFWvidmode.blueBits(mode));
	}

	public boolean setDisplayMode (DisplayMode displayMode) {
		bufferFormat = new BufferFormat( //
				displayMode.bitsPerPixel == 16 ? 5 : 8, //
				displayMode.bitsPerPixel == 16 ? 6 : 8, //
				displayMode.bitsPerPixel == 16 ? 6 : 8, //
				bufferFormat.a, bufferFormat.depth, bufferFormat.stencil, bufferFormat.samples, false);
		boolean success = createWindow(displayMode.width, displayMode.height, fullscreen);
		if (success && fullscreen) sizeChanged(displayMode.width, displayMode.height);
		return success;
	}

	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		if (fullscreen || this.fullscreen) {
			boolean success = createWindow(width, height, fullscreen);
			if (success && fullscreen) sizeChanged(width, height);
			return success;
		}

		glfwSetWindowSize(window, width, height);
		return true;
	}

	public void setTitle (String title) {
		if (title == null) title = "";
		glfwSetWindowTitle(window, title);
		this.title = title;
	}

	public void setVSync (boolean vsync) {
		this.vSync = vsync;
		glfwSwapInterval(vsync ? 1 : 0);
	}

	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	public boolean supportsExtension (String extension) {
		return glfwExtensionSupported(extension) == GL_TRUE;
	}

	public void setContinuousRendering (boolean isContinuous) {
		this.isContinuous = isContinuous;
	}

	public boolean isContinuousRendering () {
		return isContinuous;
	}

	public void requestRendering () {
		renderRequested = true;
	}

	public boolean isFullscreen () {
		return fullscreen;
	}

	/** Returns the JGLFW window handle. Note this should not be stored externally as it may change if the window is recreated to
	 * enter/exit fullscreen. */
	public long getWindow () {
		return window;
	}

	public int getX () {
		return x;
	}

	public int getY () {
		return y;
	}

	public void setPosition (int x, int y) {
		glfwSetWindowPos(window, x, y);
	}

	public void hide () {
		visible = false;
		glfwHideWindow(window);
	}

	public void show () {
		visible = true;
		glfwShowWindow(window);

		Gdx.gl.glClearColor(initialBackgroundColor.r, initialBackgroundColor.g, initialBackgroundColor.b, initialBackgroundColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		glfwSwapBuffers(window);
	}

	public boolean isHidden () {
		return !visible;
	}

	public boolean isMinimized () {
		return minimized;
	}

	public boolean isForeground () {
		return foreground;
	}

	public void minimize () {
		glfwIconifyWindow(window);
	}

	public void restore () {
		glfwRestoreWindow(window);
	}

	boolean shouldRender () {
		try {
			return renderRequested || isContinuous;
		} finally {
			renderRequested = false;
		}
	}

	static class JglfwDisplayMode extends DisplayMode {
		protected JglfwDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	@Override
	public boolean isGL30Available () {
		return false;
	}

	@Override
	public GL30 getGL30 () {
		return null;
	}
	
	@Override
	public Cursor newCursor (Pixmap pixmap, int xHotspot, int yHotspot) {
		return null;
	}

	@Override
	public void setCursor (Cursor cursor) {
	}
}
