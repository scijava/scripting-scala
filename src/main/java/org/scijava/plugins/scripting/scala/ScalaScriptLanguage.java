/*
 * #%L
 * JSR-223-compliant Scala scripting language plugin.
 * %%
 * Copyright (C) 2013 - 2017 Board of Regents of the University of
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

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.AdaptedScriptLanguage;
import org.scijava.script.ScriptLanguage;

import scala.tools.nsc.ConsoleWriter;
import scala.tools.nsc.NewLinePrintWriter;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.shell.Scripted;

/**
 * An adapter of the Scala interpreter to the SciJava scripting interface.
 * 
 * @author Curtis Rueden
 * @author Keith Schulze
 * @author Johannes Schindelin
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
		final Settings settings = new Settings();
		settings.classpath().value_$eq(getClasspath());

		Scripted eng = Scripted.apply(new Scripted.Factory(), settings,
				new NewLinePrintWriter(new ConsoleWriter(), true));

		return new ScalaScriptEngine(eng);
	}

	/** Retrieves the current classpath as a string. */
	private String getClasspath() {
		final ClassLoader cl = ClassLoader.getSystemClassLoader();
		if (!(cl instanceof URLClassLoader)) {
			log.warn("Cannot retrieve classpath from class loader of type '" +
				cl.getClass().getName() + "'");
			return System.getProperty("java.class.path");
		}
		return Arrays.stream(((URLClassLoader) cl).getURLs()).map(//
			url -> url.getPath() //
		).collect(Collectors.joining(System.getProperty("path.separator")));
	}
}
