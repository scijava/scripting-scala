/*
 * #%L
 * JSR-223-compliant Scala scripting language plugin.
 * %%
 * Copyright (C) 2013 - 2016 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.plugins.scripting.scala;

import javax.script.ScriptEngine;

import org.scijava.script.AdaptedScriptEngine;

import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.settings.MutableSettings.BooleanSetting;

/**
 * An adapter of the Scala script engine.
 *
 * @author Curtis Rueden
 */
public class ScalaScriptEngine extends AdaptedScriptEngine {

	private final IMain engine;

	public ScalaScriptEngine(final ScriptEngine engine) {
		super(engine);

		if (!(engine instanceof IMain)) {
			throw new IllegalArgumentException(//
				"Not a Scala script engine: " + engine.getClass().getName());
		}
		this.engine = (IMain) engine;

		enableClassPath();
	}

	// -- ScriptEngine methods --

	@Override
	public void put(final String key, final Object value) {
		// NB: Add a suffix to the key indicating its type.
		// This is necessary in order to properly populate the variable.
		//
		// Thanks to takawitter for this invocation:
		// https://gist.github.com/takawitter/5479445

		engine.put(key + ": " + value.getClass().getName(), value);
	}

	// -- Helper methods --

	private void enableClassPath() {
		// NB: Enable class-path-based processing.
		//
		// Without this, the engine fails with the following error:
		//
		// [init] error: error while loading Object, Missing dependency
		// 'object scala in compiler mirror', required by
		// .../lib/rt.jar(java/lang/Object.class)
		//
		// Failed to initialize compiler: object scala in compiler mirror not found.
		// ** Note that as of 2.8 scala does not assume use of the java classpath.
		// ** For the old behavior pass -usejavacp to scala, or if using a Settings
		// ** object programmatically, settings.usejavacp.value = true.
		//
		// Thanks to takawitter for this invocation:
		// https://gist.github.com/takawitter/5479445

		final BooleanSetting usejavacp = //
			(BooleanSetting) engine.settings().usejavacp();
		usejavacp.value_$eq(true);
	}

}
