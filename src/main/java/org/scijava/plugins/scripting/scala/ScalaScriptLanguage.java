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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;

import org.scijava.plugin.Plugin;
import org.scijava.script.AdaptedScriptLanguage;
import org.scijava.script.ScriptLanguage;

import scala.tools.nsc.ConsoleWriter;
import scala.tools.nsc.NewLinePrintWriter;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.Scripted;

/**
 * An adapter of the Scala interpreter to the SciJava scripting interface.
 *
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @see ScriptEngine
 */
@Plugin(type = ScriptLanguage.class, name = "Scala")
public class ScalaScriptLanguage extends AdaptedScriptLanguage {

    public ScalaScriptLanguage() {
        super("scala");
    }

    @Override
    public ScriptEngine getScriptEngine() {
        // The Scala script engine uses the "java.class.path" property from System
        // properties to resolve calls to other libraries; however, when
        // launched imagej-launcher, only the ImageJ-launcher is available in
        // System.getProperty("java.class.path"). Therefore, we check whether
        // the java.class.path and classpath according according to ClassLoader
        // are equal and update accordingly.
        updateJavaCP();

        Settings settings = new Settings();
        settings.usemanifestcp().value_$eq(true);

        // Scripted.apply sets settings.usejavacp = true
        return Scripted.apply(new Scripted.Factory(), settings,
                new NewLinePrintWriter(new ConsoleWriter(), true));
    }

    /**
     * Uses ClassLoader to generate a string of the current classpath separated by OS
     * specific separator.
     */
    private String getClasspath() {
        return Arrays.stream(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs())
            .map(url -> url.getPath())
            .collect(Collectors.joining(System.getProperty("path.separator")));
    }

    /**
     *  Checks whether "java.class.path" property in System properties and
     *  classpath according to ClassLoader are equals and updates java.class.path
     *  if necessary.
     */
    private void updateJavaCP() {
        String cp = getClasspath();

        if (!Objects.equals(System.getProperty("java.class.path"), cp)) {
            Properties p = new Properties(System.getProperties());
            p.setProperty("java.class.path", cp);

            System.setProperties(p);
        }
    }
}
