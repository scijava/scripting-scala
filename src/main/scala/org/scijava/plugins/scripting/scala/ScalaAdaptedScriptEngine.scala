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

import java.io.{OutputStream, Reader, StringWriter, Writer}
import javax.script.*
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.matching.Regex

/**
 * Adapted Scala ScriptEngine
 *
 * @author Jarek Sacha
 * @author Keith Schulze
 * @see ScriptEngine
 */
class ScalaAdaptedScriptEngine(engine: ScriptEngine) extends AbstractScriptEngine:

  import ScalaAdaptedScriptEngine.*

  private val buffer = new Array[Char](8192)

  @throws[ScriptException]
  override def eval(reader: Reader, context: ScriptContext): AnyRef = eval(stringFromReader(reader), context)

  @throws[ScriptException]
  override def eval(script: String, context: ScriptContext): AnyRef =
    emulateBinding(context)
    val r = evalInner(script, context)
    // Scala returns `Unit` when no value is returned. Script Engine (or the
    // Java side) expects `null` when no value was returned.
    // Anything else return as is.
    r match
      case _: Unit => null
      case x       => x

  private def emulateBinding(context: ScriptContext): Unit =

    // Scala 3.2.2 ignores bindings, emulate binding using setup script
    // Create a line with variable declaration for each binding item
    val lines =
      for
        scope    <- context.getScopes.asScala
        bindings <- Option(context.getBindings(scope)).map(_.asScala) // bindings in context can be null
      yield {
        for (name, value) <- bindings yield {
          if isValidVariableName(name) then
            val validName = addBackticksIfNeeded(name)
            value match
              case v: Double  => s"val $validName : Double = ${v}d"
              case v: Float   => s"val $validName : Float = ${v}f"
              case v: Long    => s"val $validName : Long = ${v}L"
              case v: Int     => s"val $validName : Int = $v"
              case v: Char    => s"val $validName : Char = '$v'"
              case v: Short   => s"val $validName : Short = $v"
              case v: Byte    => s"val $validName : Byte = $v"
              case v: Boolean => s"val $validName : Int = $v"
              case v: AnyRef =>
                _transfer = v
                val typeName      = Option(v).map(_.getClass.getCanonicalName).getOrElse("AnyRef")
                val validTypeName = addBackticksIfNeeded(typeName)
                s"""
                   |val $validName : $validTypeName = {
                   |  val t = org.scijava.plugins.scripting.scala.ScalaAdaptedScriptEngine._transfer
                   |  t.asInstanceOf[$validTypeName]
                   |}""".stripMargin
              case v: Unit =>
                throw ScriptException(s"Unsupported type for bind variable $name: ${v.getClass}")
          else
            "" // ignore if name is not a variable
        }
      }

    val script = lines
      .flatten
      .filter(_.nonEmpty)
      .mkString("\n")

    if script.nonEmpty then
      evalInner(script, context)

  end emulateBinding

  private def evalInner(script: String, context: ScriptContext) =
    class WriterOutputStream(w: Writer) extends OutputStream:
      override def write(b: Int): Unit = w.write(b)

    // Redirect output to writes provided by context
    Console.withOut(WriterOutputStream(context.getWriter)) {
      Console.withErr(WriterOutputStream(context.getErrorWriter)) {
        engine.eval(script, context)
      }
    }

  private def stringFromReader(in: Reader) =
    val out = new StringWriter()
    var n   = in.read(buffer)
    while n > -1 do
      out.write(buffer, 0, n)
      n = in.read(buffer)
    in.close()
    out.toString

  override def createBindings(): Bindings = engine.createBindings

  override def getFactory: ScriptEngineFactory = engine.getFactory

  override def get(key: String): AnyRef =
    // First try to get value from bindings
    var value = super.get(key)

    // NB: Extracting values from Scala Script Engine are a little tricky.
    // Values (variables) initialised or computed in the script are
    // not added to the bindings of the CompiledScript AFAICT. Therefore
    // the only way to extract them is to evaluate the variable and
    // capture the return. If it evaluates to null or throws a
    // a ScriptException, we simply return null.
    if value == null then
      try
        value = evalInner(key, getContext)
      catch
        case _: ScriptException =>
        // HACK: Explicitly ignore ScriptException, which arises if
        // key is not found. This feels bad because it fails silently
        // for the user, but it mimics behaviour in other script langs.

    value
  end get

end ScalaAdaptedScriptEngine

object ScalaAdaptedScriptEngine:
  private lazy val variableNamePattern = """^[a-zA-Z_$][a-zA-Z_$0-9]*$""".r
  private val scala3Keywords = Seq(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "enum",
    "export",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "given",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "then",
    "throw",
    "trait",
    "true",
    "try",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )

  /** Do not use externally despite it is declared public. IT is public so it is accessible from scripts */
  // noinspection ScalaWeakerAccess
  var _transfer: Object = _

  private def isValidVariableName(name: String): Boolean = variableNamePattern.matches(name)

  private[scala] def addBackticksIfNeeded(referenceName: String): String =
    referenceName
      .split("\\.")
      .map(n => if scala3Keywords.contains(n) then s"`$n`" else n)
      .mkString(".")

end ScalaAdaptedScriptEngine
