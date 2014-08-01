/*
 * #%L
 * JSR-223-compliant Scala scripting language plugin.
 * %%
 * Copyright (C) 2013 - 2014 Board of Regents of the University of
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

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map.Entry;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.scijava.script.AbstractScriptEngine;

import scala.collection.immutable.List;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

/**
 * A Scala interpreter for ImageJ.
 * 
 * @author Johannes Schindelin
 */
public class ScalaScriptEngine extends AbstractScriptEngine {

	{
		engineScopeBindings = new ScalaBindings();
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		try {
			return interpreter().interpret(script);
		}
		catch (final Exception e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(final Reader reader) throws ScriptException {
		try {
			final BufferedReader bufferedReader = new BufferedReader(reader);
			final StringWriter writer = new StringWriter();
			for (;;) {
				final String line = bufferedReader.readLine();
				if (line == null) break;
				writer.write(line);
				writer.write("\n");
			}
			return interpreter().interpret(writer.toString());
		}
		catch (final Exception e) {
			throw new ScriptException(e);
		}
	}

	private IMain interpreter() {
		final ScriptContext context = getContext();
		final Writer writer = context.getWriter();

		final Settings settings = new Settings();
		settings.usejavacp().tryToSet(List.make(1, "true"));
		final PrintWriter out = writer == null ? null :
			(writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer));
		final IMain interpreter = out == null ? new IMain(settings) : new IMain(settings, out);

		for (final Entry<String, Object> entry : engineScopeBindings.entrySet()) {
			final String name = entry.getKey();
			final Object value = entry.getValue();
			interpreter.bind(name, value.getClass().getCanonicalName(), value, List.make(0, ""));
		}
		
		return interpreter;
	}
}
