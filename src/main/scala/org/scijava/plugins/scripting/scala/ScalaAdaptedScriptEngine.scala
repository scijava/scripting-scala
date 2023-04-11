package org.scijava.plugins.scripting.scala

import java.io.{OutputStream, Reader, StringWriter, Writer}
import javax.script.*
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

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
    evalInner(script, context)

  private def emulateBinding(context: ScriptContext): Unit =

    // Scala 3.2.2 ignores bindings, emulate binding using setup script
    // Create a line with variable declaration for each binding item
    val lines =
      for
        scope    <- context.getScopes.asScala
        bindings <- Option(context.getBindings(scope)).map(_.asScala) // bindings in context can be null
      yield {
        for (name, value) <- bindings yield {
          value match
            case v: Double  => s"val $name : Double = ${v}d"
            case v: Float   => s"val $name : Float = ${v}f"
            case v: Long    => s"val $name : Long = ${v}L"
            case v: Int     => s"val $name : Int = $v"
            case v: Char    => s"val $name : Char = '$v'"
            case v: Short   => s"val $name : Short = $v"
            case v: Byte    => s"val $name : Byte = $v"
            case v: Boolean => s"val $name : Int = $v"
            case o: AnyRef if isValidVariableName(name) =>
              _transfer = o
              val typeName = Option(o).map(_.getClass.getCanonicalName).getOrElse("AnyRef")
              s"""
                 |val $name : $typeName = {
                 |  val t = org.scijava.plugins.scripting.scala.ScalaAdaptedScriptEngine._transfer
                 |  t.asInstanceOf[$typeName]
                 |}""".stripMargin
            case _: AnyRef => "" // ignore if name is not a variable
            case v: Unit =>
              throw ScriptException(s"Unsupported type for bind variable $name: ${v.getClass}")
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

    // NB: Extracting values from Scala Script Engine are a little tricky.// NB: Extracting values from Scala Script Engine are a little tricky.
    // Values (variables) initialised or computed in the script are// Values (variables) initialised or computed in the script are
    // not added to the bindings of the CompiledScript AFAICT. Therefore// not added to the bindings of the CompiledScript AFAICT. Therefore
    // the only way to extract them is to evaluate the variable and// the only way to extract them is to evaluate the variable and
    // capture the return. If it evaluates to null or throws a// capture the return. If it evaluates to null or throws a
    // a ScriptException, we simply return null.// a ScriptException, we simply return null.
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

  /** Do not use externally despite it is declared public. IT is public so it is accessible from scripts */
  // noinspection ScalaWeakerAccess
  var _transfer: Object = _

  private def isValidVariableName(name: String): Boolean = variableNamePattern.matches(name)
end ScalaAdaptedScriptEngine
