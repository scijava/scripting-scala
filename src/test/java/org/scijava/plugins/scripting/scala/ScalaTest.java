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

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

/**
 * Scala unit tests.
 *
 * @author Johannes Schindelin
 * @author Keith Schulze
 */
public class ScalaTest {

	private ScriptService scriptService;
	private ScriptEngine engine;

	@Before
	public void setUp() {
		final Context context = new Context(ScriptService.class);
		scriptService = context.getService(ScriptService.class);

		final ScriptLanguage language =
			scriptService.getLanguageByExtension("scala");
		engine = language.getScriptEngine();
	}

	/**
	 * Test whether we can evaluate something in the ScalaScriptEngine.
	 */
	@Test
	public void testBasic() throws Exception {

		final SimpleScriptContext ssc = new SimpleScriptContext();
		final StringWriter writer = new StringWriter();
		ssc.setWriter(writer);

		final String script = "print(\"3\");";
		engine.eval(script, ssc);
		assertEquals("3", writer.toString());
	}

	/**
	 * Test whether input parameters resolve to the correct type.
	 */
	@Test
	public void testParameterType() throws ScriptException, ExecutionException, InterruptedException {

		String ls = System.getProperty("line.separator");
		// We create a script that requests injection of a @ScriptService
		// via a Script parameter. Then use Scala reflection tools to check
		// the runtime TypeTag of the ScriptService. We expect this to return
		// org.scijava.script.ScriptService rather than Object.
		final String script = String.join(
			ls,
			"// @ScriptService ss",
			"// @OUTPUT String ssType",
			"import scala.reflect.runtime.{universe => ru}",
			"def getTypeTag[T: ru.TypeTag](v: T) = ru.typeTag[T]",
			"val ssType: String = getTypeTag(ss).toString"
		);

		final ScriptModule sm = scriptService.run("test.scala", script, true).get();

		final Object actual = sm.getOutput("ssType");
		final String expected = "TypeTag[org.scijava.script.ScriptService]";

		assertEquals(expected, actual);
	}

	@Test
	public void testParameters() throws ScriptException, ExecutionException, InterruptedException {

		String ls = System.getProperty("line.separator");
		final String script = String.join(
			ls,
			"// @ScriptService ss",
			"// @OUTPUT String lang",
			"val lang = ss.getLanguageByName(\"scala\").getLanguageName()"
		);

		final ScriptModule sm = scriptService.run("test.scala", script, true).get();

		final Object actual = sm.getOutput("lang");
		final String expected = scriptService.getLanguageByName("scala").getLanguageName();

		assertEquals(expected, actual);
	}
}
