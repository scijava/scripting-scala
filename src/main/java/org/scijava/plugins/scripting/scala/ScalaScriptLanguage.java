/*
 * #%L
 * JSR-223-compliant Scala scripting language plugin.
 * %%
 * Copyright (C) 2013 - 2023 SciJava developers.
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

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.AdaptedScriptLanguage;
import org.scijava.script.ScriptLanguage;

/**
 * An adapter of the Scala interpreter to the SciJava scripting interface.
 * 
 * @author Curtis Rueden
 * @author Keith Schulze
 * @author Johannes Schindelin
 * @author Jarek Sacha
 * @see ScriptEngine
 */
@Plugin(type = ScriptLanguage.class, name = "Scala")
public class ScalaScriptLanguage extends AdaptedScriptLanguage {

	@Parameter
	private LogService log;

	public ScalaScriptLanguage() {
		super("scala");
	}

	@Override
	public ScriptEngine getScriptEngine() {
		final ScriptEngine eng = new dotty.tools.repl.ScriptEngine();
		return new ScalaAdaptedScriptEngine(eng);
	}
}
