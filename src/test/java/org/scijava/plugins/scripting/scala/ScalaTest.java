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

import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Scala unit tests.
 *
 * @author Johannes Schindelin
 * @author Keith Schulze
 * @author Jarek Sacha
 */
public class ScalaTest {

    private ScriptEngine getEngine(Context context) {
        final ScriptService scriptService = context.getService(ScriptService.class);
        final ScriptLanguage language = scriptService.getLanguageByExtension("scala");
        return language.getScriptEngine();
    }

    @Test
    public void testBasic() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {

            final ScriptEngine engine = getEngine(context);

            final SimpleScriptContext ssc = new SimpleScriptContext();
            final StringWriter writer = new StringWriter();
            ssc.setWriter(writer);

            final String script = "print(\"3\")";
            engine.eval(script, ssc);
            assertEquals("3", writer.toString());
        }
    }

    @Test
    public void testEmptyReturnValue() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);
            final ScriptModule m = scriptService.run("hello.scala", "print(\"3\")", true).get();
            final Void expected = null;
            final Object actual = m.getReturnValue();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutDouble() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final double expected = 7.1d;
            engine.put("v", expected);
            final String script = "val v1:Double = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutFloat() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final float expected = 7.1f;
            engine.put("v", expected);
            final String script = "val v1:Float = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutLong() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final long expected = 7L;
            engine.put("v", expected);
            final String script = "val v1:Long = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutChar() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final char expected = 'q';
            engine.put("v", expected);
            final String script = "val v1:Char = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutShort() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final short expected = 512;
            engine.put("v", expected);
            final String script = "val v1:Short = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutByte() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final byte expected = -127;
            engine.put("v", expected);
            final String script = "val v1:Byte = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPutString() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final String expected = "Ala ma kota";
            engine.put("v", expected);
            final String script = "val v1:String = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testPutInt() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            final int expected = 7;
            engine.put("v", expected);
            final String script = "val v1:Int = v";
            engine.eval(script);
            final Object actual = engine.get("v1");
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testLocals() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);

            final ScriptLanguage language = scriptService.getLanguageByExtension("scala");
            final ScriptEngine engine = language.getScriptEngine();
            assertEquals("org.scijava.plugins.scripting.scala.ScalaAdaptedScriptEngine", engine.getClass().getName());
            engine.put("hello", 17);
            assertEquals("17", engine.eval("hello").toString());
            assertEquals("17", engine.get("hello").toString());

//             With Scala 3.2.2 cannot reset bindings correctly, will skip the ret of the test
//            final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
//            bindings.clear();
//            assertNull(engine.get("hello"));
        }
    }

    @Test
    public void testParameters() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);

            final String script = "" + //
                    "#@ScriptService ss\n" + //
                    "#@OUTPUT String language\n" + //
                    "val language = ss.getLanguageByName(\"scala\").getLanguageName()\n";
            final ScriptModule m = scriptService.run("hello.scala", script, true).get();

            final Object actual = m.getOutput("language");
            final String expected =
                    scriptService.getLanguageByName("scala").getLanguageName();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void test2Inputs() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptEngine engine = getEngine(context);

            engine.put("a", 2);
            engine.put("b", 5);
            engine.eval("val c = a + b");
            final Object actual = engine.get("c");
            assertEquals(7, actual);
        }
    }

    @Test
    public void testImportsRetained() throws Exception {
        try (final Context context = new Context(ScriptService.class)) {
            final ScriptService scriptService = context.getService(ScriptService.class);
            final ScriptEngine engine = scriptService.getLanguageByName("scala").getScriptEngine();
            final String script = "" +
                    "import org.scijava.util.VersionUtils\n" +
                    "VersionUtils.getVersion(classOf[VersionUtils])\n";
            final Object result = engine.eval(script);
            assertTrue(result instanceof String);
            final String version = (String) result;
            assertTrue(version, version.matches("\\d+\\.\\d+\\.\\d"));

            final String script2 = "VersionUtils.getVersion(classOf[VersionUtils])\n";
            final Object result2 = engine.eval(script2);
            assertEquals(result, result2);
        }
    }
}
