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

import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Scala unit tests.
 *
 * @author Johannes Schindelin
 * @author Keith Schulze
 */
public class ScalaTest {

    @Test
    public void testBasic() throws Exception {
        final Context context = new Context(ScriptService.class);
        final ScriptService scriptService = context.getService(ScriptService.class);

        final ScriptLanguage language =
                scriptService.getLanguageByExtension("scala");
        final ScriptEngine engine = language.getScriptEngine();

        final SimpleScriptContext ssc = new SimpleScriptContext();
        final StringWriter writer = new StringWriter();
        ssc.setWriter(writer);

        final String script = "print(\"3\");";
        engine.eval(script, ssc);
        assertEquals("3", writer.toString());
    }

    @Test
    public void testLocals() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);

            final ScriptLanguage language = scriptService.getLanguageByExtension("scala");
            final ScriptEngine engine = language.getScriptEngine();
            assertEquals("org.scijava.plugins.scripting.scala.ScalaScriptEngine", engine.getClass().getName());
            engine.put("hello", 17);
            assertEquals("17", engine.eval("hello").toString());
            assertEquals("17", engine.get("hello").toString());

            final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.clear();
            assertNull(engine.get("hello"));
        }
    }

    @Test
    public void testParameters() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);

            final String script = "" + //
                    "#@ScriptService ss\n" + //
                    "#@OUTPUT String language\n" + //
                    "val sst = ss.asInstanceOf[org.scijava.script.ScriptService]\n" + // `ss` needs cast from `Object`
                    "val language = sst.getLanguageByName(\"scala\").getLanguageName()\n";
            final ScriptModule m = scriptService.run("hello.scala", script, true).get();

            final Object actual = m.getOutput("language");
            final String expected =
                    scriptService.getLanguageByName("scala").getLanguageName();
            assertEquals(expected, actual);
        }
    }
}
