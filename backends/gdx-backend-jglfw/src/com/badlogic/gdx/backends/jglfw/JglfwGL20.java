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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.opengl.ARBES2Compatibility;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class JglfwGL20 implements com.badlogic.gdx.graphics.GL20 {
	private ByteBuffer buffer = null;
	private FloatBuffer floatBuffer = null;
	private IntBuffer intBuffer = null;

	private void ensureBufferCapacity (int numBytes) {
		if (buffer == null || buffer.capacity() < numBytes) {
			buffer = com.badlogic.gdx.utils.BufferUtils.newByteBuffer(numBytes);
			floatBuffer = buffer.asFloatBuffer();
			intBuffer = buffer.asIntBuffer();
		}
	}

	private FloatBuffer toFloatBuffer (float v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		floatBuffer.clear();
		com.badlogic.gdx.utils.BufferUtils.copy(v, floatBuffer, count, offset);
		return floatBuffer;
	}

	private IntBuffer toIntBuffer (int v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		intBuffer.clear();
		com.badlogic.gdx.utils.BufferUtils.copy(v, count, offset, intBuffer);
		return intBuffer;
	}
	
	private IntBuffer toIntBuffer (int v) {
		ensureBufferCapacity(4);
		intBuffer.put(0, v);
		intBuffer.position(0);
		intBuffer.limit(1);
		return intBuffer;
	}
	
	public void glActiveTexture (int texture) {
		GL13.glActiveTexture(texture);
	}

	public void glBindTexture (int target, int texture) {
		GL11.glBindTexture(target, texture);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public void glClear (int mask) {
		GL11.glClear(mask);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf (float depth) {
		GL11.glClearDepth(depth);
	}

	public void glClearStencil (int s) {
		GL11.glClearStencil(s);
	}

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, (ByteBuffer)data);
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GL13.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, (ByteBuffer)data);
	}

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public void glCullFace (int mode) {
		GL11.glCullFace(mode);
	}

	public void glDeleteTextures (int n, IntBuffer textures) {
		GL11.glDeleteTextures(textures);
	}
	
	public void glDeleteTexture (int texture) {
		glDeleteTextures(1, toIntBuffer(texture));
	}

	public void glDepthFunc (int func) {
		GL11.glDepthFunc(func);
	}

	public void glDepthMask (boolean flag) {
		GL11.glDepthMask(flag);
	}

	public void glDepthRangef (float zNear, float zFar) {
		GL11.glDepthRange(zNear, zFar);
	}

	public void glDisable (int cap) {
		GL11.glDisable(cap);
	}

	public void glDrawArrays (int mode, int first, int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, (ShortBuffer)indices);
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, ((ByteBuffer)indices).asShortBuffer()); // FIXME yay...
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE)
			GL11.glDrawElements(mode, (ByteBuffer)indices);
		else if (indices instanceof IntBuffer && type == com.badlogic.gdx.graphics.GL20.GL_INT)
			GL11.glDrawElements(mode, (IntBuffer)indices);
		else
			throw new GdxRuntimeException("Can't use " + indices.getClass().getName()
					+ " with this method. Use IntBuffer, ShortBuffer or ByteBuffer instead. Blame LWJGL");
	}

	public void glEnable (int cap) {
		GL11.glEnable(cap);
	}

	public void glFinish () {
		GL11.glFinish();
	}

	public void glFlush () {
		GL11.glFlush();
	}

	public void glFrontFace (int mode) {
		GL11.glFrontFace(mode);
	}

	public void glGenTextures (int n, IntBuffer textures) {
		GL11.glGenTextures(textures);
	}
	
	public int glGenTexture () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenTextures(1, intBuffer);
		return intBuffer.get(0);
	}

	public int glGetError () {
		return GL11.glGetError();
	}

	public void glGetIntegerv (int pname, IntBuffer params) {
		GL11.glGetIntegerv(pname, params);
	}

	public String glGetString (int name) {
		return GL11.glGetString(name);
	}

	public void glHint (int target, int mode) {
		GL11.glHint(target, mode);
	}

	public void glLineWidth (float width) {
		GL11.glLineWidth(width);
	}

	public void glPixelStorei (int pname, int param) {
		GL11.glPixelStorei(pname, param);
	}

	public void glPolygonOffset (float factor, float units) {
		GL11.glPolygonOffset(factor, units);
	}

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL");
	}

	public void glScissor (int x, int y, int width, int height) {
		GL11.glScissor(x, y, width, height);
	}

	public void glStencilFunc (int func, int ref, int mask) {
		GL11.glStencilFunc(func, ref, mask);
	}

	public void glStencilMask (int mask) {
		GL11.glStencilMask(mask);
	}

	public void glStencilOp (int fail, int zfail, int zpass) {
		GL11.glStencilOp(fail, zfail, zpass);
	}

	public void glTexImage2D (int target, int level, int internalFormat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		if (pixels == null)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glTexParameterf (int target, int pname, float param) {
		GL11.glTexParameterf(target, pname, param);
	}

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glViewport (int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

	public void glGetFloatv (int pname, FloatBuffer params) {
		GL11.glGetFloatv(pname, params);
	}

	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glGetTexParameterfv(target, pname, params);
	}

	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glTexParameterfv(target, pname, params);
	}

	public void glBindBuffer (int target, int buffer) {
		GL15.glBindBuffer(target, buffer);
	}

	public void glBufferData (int target, int size, Buffer data, int usage) {
		if (data == null)
			GL15.glBufferData(target, size, usage);
		else if (data instanceof ByteBuffer)
			GL15.glBufferData(target, (ByteBuffer)data, usage);
		else if (data instanceof IntBuffer)
			GL15.glBufferData(target, (IntBuffer)data, usage);
		else if (data instanceof FloatBuffer)
			GL15.glBufferData(target, (FloatBuffer)data, usage);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferData(target, (DoubleBuffer)data, usage);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferData(target, (ShortBuffer)data, usage);
	}

	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		if (data == null)
			throw new GdxRuntimeException("Using null for the data not possible, blame LWJGL");
		else if (data instanceof ByteBuffer)
			GL15.glBufferSubData(target, offset, (ByteBuffer)data);
		else if (data instanceof IntBuffer)
			GL15.glBufferSubData(target, offset, (IntBuffer)data);
		else if (data instanceof FloatBuffer)
			GL15.glBufferSubData(target, offset, (FloatBuffer)data);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferSubData(target, offset, (DoubleBuffer)data);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferSubData(target, offset, (ShortBuffer)data);
	}

	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GL15.glDeleteBuffers(buffers);
	}
	
	public void glDeleteBuffer (int buffer) {
		glDeleteBuffers(1, toIntBuffer(buffer));
	}

	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL15.glGetBufferParameteriv(target, pname, params);
	}

	public void glGenBuffers (int n, IntBuffer buffers) {
		GL15.glGenBuffers(buffers);
	}
	
	public int glGenBuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenBuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glGetTexParameteriv(target, pname, params);
	}

	public boolean glIsBuffer (int buffer) {
		return GL15.glIsBuffer(buffer);
	}

	public boolean glIsEnabled (int cap) {
		return GL11.glIsEnabled(cap);
	}

	public boolean glIsTexture (int texture) {
		return GL11.glIsTexture(texture);
	}

	public void glTexParameteri (int target, int pname, int param) {
		GL11.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glTexParameteriv(target, pname, params);
	}

	public void glDrawElements (int mode, int count, int type, int indices) {
		GL11.glDrawElements(mode, count, type, indices);
	}

	public void glAttachShader (int program, int shader) {
		GL20.glAttachShader(program, shader);
	}

	public void glBindAttribLocation (int program, int index, String name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	public void glBindFramebuffer (int target, int framebuffer) {
		EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
	}

	public void glBindRenderbuffer (int target, int renderbuffer) {
		EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
	}

	public void glBlendColor (float red, float green, float blue, float alpha) {
		GL14.glBlendColor(red, green, blue, alpha);
	}

	public void glBlendEquation (int mode) {
		GL14.glBlendEquation(mode);
	}

	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GL20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public int glCheckFramebufferStatus (int target) {
		return EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
	}

	public void glCompileShader (int shader) {
		GL20.glCompileShader(shader);
	}

	public int glCreateProgram () {
		return GL20.glCreateProgram();
	}

	public int glCreateShader (int type) {
		return GL20.glCreateShader(type);
	}

	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffers);
	}
	
	public void glDeleteFramebuffer (int framebuffer) {
		glDeleteFramebuffers(1, toIntBuffer(framebuffer));
	}

	public void glDeleteProgram (int program) {
		GL20.glDeleteProgram(program);
	}

	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffers);
	}
	
	public void glDeleteRenderbuffer (int renderbuffer) {
		glDeleteRenderbuffers(1, toIntBuffer(renderbuffer));
	}

	public void glDeleteShader (int shader) {
		GL20.glDeleteShader(shader);
	}

	public void glDetachShader (int program, int shader) {
		GL20.glDetachShader(program, shader);
	}

	public void glDisableVertexAttribArray (int index) {
		GL20.glDisableVertexAttribArray(index);
	}

	public void glEnableVertexAttribArray (int index) {
		GL20.glEnableVertexAttribArray(index);
	}

	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
	}

	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
	}

	public void glGenerateMipmap (int target) {
		EXTFramebufferObject.glGenerateMipmapEXT(target);
	}

	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		EXTFramebufferObject.glGenFramebuffersEXT(framebuffers);
	}
	
	public int glGenFramebuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenFramebuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		EXTFramebufferObject.glGenRenderbuffersEXT(renderbuffers);
	}
	
	public int glGenRenderbuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenRenderbuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		return GL20.glGetActiveAttrib(program, index, size, (IntBuffer)type);
	}

	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		return GL20.glGetActiveUniform(program, index, size, (IntBuffer)type);
	}

	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GL20.glGetAttachedShaders(program, (IntBuffer)count, shaders);
	}

	public int glGetAttribLocation (int program, String name) {
		return GL20.glGetAttribLocation(program, name);
	}

	public void glGetBooleanv (int pname, Buffer params) {
		GL11.glGetBooleanv(pname, (ByteBuffer)params);
	}

	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		EXTFramebufferObject.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params);
	}

	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL20.glGetProgramiv(program, pname, params);
	}

	public String glGetProgramInfoLog (int program) {
		return GL20.glGetProgramInfoLog(program);
	}

	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		EXTFramebufferObject.glGetRenderbufferParameterivEXT(target, pname, params);
	}

	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL20.glGetShaderiv(shader, pname, params);
	}

	public String glGetShaderInfoLog (int shader) {
		return GL20.glGetShaderInfoLog(shader);
	}

	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		ARBES2Compatibility.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
	}

	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GL20.glGetUniformfv(program, location, params);
	}

	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GL20.glGetUniformiv(program, location, params);
	}

	public int glGetUniformLocation (int program, String name) {
		return GL20.glGetUniformLocation(program, name);
	}

	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GL20.glGetVertexAttribfv(index, pname, params);
	}

	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GL20.glGetVertexAttribiv(index, pname, params);
	}

	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		GL20.glGetVertexAttribPointerv(index, pname, (ByteBuffer)pointer);
	}

	public boolean glIsFramebuffer (int framebuffer) {
		return EXTFramebufferObject.glIsFramebufferEXT(framebuffer);
	}

	public boolean glIsProgram (int program) {
		return GL20.glIsProgram(program);
	}

	public boolean glIsRenderbuffer (int renderbuffer) {
		return EXTFramebufferObject.glIsRenderbufferEXT(renderbuffer);
	}

	public boolean glIsShader (int shader) {
		return GL20.glIsShader(shader);
	}

	public void glLinkProgram (int program) {
		GL20.glLinkProgram(program);
	}

	public void glReleaseShaderCompiler () {
		ARBES2Compatibility.glReleaseShaderCompiler();
	}

	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		EXTFramebufferObject.glRenderbufferStorageEXT(target, internalformat, width, height);
	}

	public void glSampleCoverage (float value, boolean invert) {
		GL13.glSampleCoverage(value, invert);
	}

	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		ARBES2Compatibility.glShaderBinary(shaders, binaryformat, (ByteBuffer)binary);
	}

	public void glShaderSource (int shader, String string) {
		GL20.glShaderSource(shader, string);
	}

	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GL20.glStencilFuncSeparate(face, func, ref, mask);
	}

	public void glStencilMaskSeparate (int face, int mask) {
		GL20.glStencilMaskSeparate(face, mask);
	}

	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GL20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	public void glUniform1f (int location, float x) {
		GL20.glUniform1f(location, x);
	}

	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GL20.glUniform1fv(location, v);
	}

	public void glUniform1fv (int location, int count, float[] v, int offset) {
		glUniform1fv(location, count, toFloatBuffer(v, offset, count));
	}
	
	public void glUniform1i (int location, int x) {
		GL20.glUniform1i(location, x);
	}

	public void glUniform1iv (int location, int count, IntBuffer v) {
		GL20.glUniform1iv(location, v);
	}
	
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		glUniform1iv(location, count, toIntBuffer(v, offset, count));
	}

	public void glUniform2f (int location, float x, float y) {
		GL20.glUniform2f(location, x, y);
	}

	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GL20.glUniform2fv(location, v);
	}
	
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		glUniform2fv(location, count, toFloatBuffer(v, offset, count << 1));
	}

	public void glUniform2i (int location, int x, int y) {
		GL20.glUniform2i(location, x, y);
	}

	public void glUniform2iv (int location, int count, IntBuffer v) {
		GL20.glUniform2iv(location, v);
	}
	
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		glUniform2iv(location, count, toIntBuffer(v, offset, count<<1));
	}

	public void glUniform3f (int location, float x, float y, float z) {
		GL20.glUniform3f(location, x, y, z);
	}

	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GL20.glUniform3fv(location, v);
	}
	
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		glUniform3fv(location, count, toFloatBuffer(v, offset, count*3));
	}

	public void glUniform3i (int location, int x, int y, int z) {
		GL20.glUniform3i(location, x, y, z);
	}

	public void glUniform3iv (int location, int count, IntBuffer v) {
		GL20.glUniform3iv(location, v);
	}
	
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		glUniform3iv(location, count, toIntBuffer(v, offset, count*3));
	}

	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL20.glUniform4f(location, x, y, z, w);
	}

	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GL20.glUniform4fv(location, v);
	}
	
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		glUniform4fv(location, count, toFloatBuffer(v, offset, count << 2));
	}

	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL20.glUniform4i(location, x, y, z, w);
	}

	public void glUniform4iv (int location, int count, IntBuffer v) {
		GL20.glUniform4iv(location, v);
	}
	
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		glUniform4iv(location, count, toIntBuffer(v, offset, count << 2));
	}

	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix2fv(location, transpose, value);
	}
	
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix2fv(location, count, transpose, toFloatBuffer(value, offset, count << 2));
	}

	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix3fv(location, transpose, value);
	}
	
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix3fv(location, count, transpose, toFloatBuffer(value, offset, count * 9));
	}

	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL20.glUniformMatrix4fv(location, transpose, value);
	}
	
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix4fv(location, count, transpose, toFloatBuffer(value, offset, count << 4));
	}

	public void glUseProgram (int program) {
		GL20.glUseProgram(program);
	}

	public void glValidateProgram (int program) {
		GL20.glValidateProgram(program);
	}

	public void glVertexAttrib1f (int indx, float x) {
		GL20.glVertexAttrib1f(indx, x);
	}

	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib1fv(indx, values);
	}

	public void glVertexAttrib2f (int indx, float x, float y) {
		GL20.glVertexAttrib2f(indx, x, y);
	}

	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib2fv(indx, values);
	}

	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GL20.glVertexAttrib3f(indx, x, y, z);
	}

	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib3fv(indx, values);
	}

	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GL20.glVertexAttrib4f(indx, x, y, z, w);
	}

	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		GL20.glVertexAttrib4fv(indx, values);
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer buffer) {
		if (buffer instanceof ByteBuffer) {
			GL20.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer)buffer);
		} else if (buffer instanceof IntBuffer) {
			if (type == GL_INT)
				GL20.glVertexAttribPointer(indx, size, normalized, stride, (IntBuffer)buffer);
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
						+ " with this method.");
		} else if (buffer instanceof FloatBuffer) {
			if (type == GL_FLOAT)
				GL20.glVertexAttribPointer(indx, size, normalized, stride, (FloatBuffer)buffer);
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
						+ " with this method.");
		} else if (buffer instanceof DoubleBuffer) {
			if (type == GL_FLOAT)
				GL20.glVertexAttribPointer(indx, size, normalized, stride, (DoubleBuffer)buffer);
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
						+ " with this method.");
		} else
			throw new GdxRuntimeException("Can't use " + buffer.getClass().getName()
					+ " with this method. Use ByteBuffer instead. Blame LWJGL");
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
}
