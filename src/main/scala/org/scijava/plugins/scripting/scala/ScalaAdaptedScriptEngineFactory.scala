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

package org.scijava.plugins.scripting.scala

import org.scijava.plugins.scripting.scala.ScalaAdaptedScriptEngine

import java.util
import javax.script.{ScriptEngine, ScriptEngineFactory}

/**
 * A factory for ScalaAdaptedScriptEngine.
 *
 * @author Jarek Sacha
 * @see ScriptEngineFactory
 */
class ScalaAdaptedScriptEngineFactory extends ScriptEngineFactory:

  private val factory = new dotty.tools.repl.ScriptEngine.Factory

  /**
   * Returns an instance of the `ScalaAdaptedScriptEngine`.
   * A new instance is returned.
   *
   * @return A new `ScalaAdaptedScriptEngine` instance.
   */
  override def getScriptEngine = new ScalaAdaptedScriptEngine(factory.getScriptEngine)

  override def getEngineName: String                         = factory.getEngineName
  override def getEngineVersion: String                      = factory.getEngineVersion
  override def getExtensions: util.List[String]              = factory.getExtensions
  override def getLanguageName: String                       = factory.getLanguageName
  override def getLanguageVersion: String                    = factory.getLanguageVersion
  override def getMimeTypes: util.List[String]               = factory.getMimeTypes
  override def getNames: util.List[String]                   = factory.getNames
  override def getOutputStatement(toDisplay: String): String = factory.getOutputStatement(toDisplay)
  override def getParameter(key: String): AnyRef             = factory.getParameter(key)
  override def getMethodCallSyntax(obj: String, m: String, args: String*): String =
    factory.getMethodCallSyntax(obj, m, args*)
  override def getProgram(statements: String*): String = factory.getProgram(statements*)
