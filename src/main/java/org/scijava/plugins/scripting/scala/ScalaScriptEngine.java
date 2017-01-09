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

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.script.AdaptedScriptEngine;

/**
 * Scala interpreter
 *
 * @author Keith Schulze
 * @see ScriptEngine
 */
public class ScalaScriptEngine extends AdaptedScriptEngine {

    public ScalaScriptEngine(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public Object get(String key) {
        // First try to get value from bindings
        Object value = super.get(key);

        // NB: Extracting values from Scala Script Engine are a little tricky.
        // Values (variables) initialised or computed in the script are
        // not added to the bindings of the CompiledScript AFAICT. Therefore
        // the only way to extract them is to evaluate the variable and
        // capture the return. If it evaluates to null or throws a
        // a ScriptException, we simply return null.
        if (value == null) try {
            value = super.eval(key);
        } catch (ScriptException ignored) {
            // HACK: Explicitly ignore ScriptException, which arises if
            // key is not found. This feels bad because it fails silently
            // for the user, but it mimics behaviour in other script langs.
        }

        return value;
    }
}
