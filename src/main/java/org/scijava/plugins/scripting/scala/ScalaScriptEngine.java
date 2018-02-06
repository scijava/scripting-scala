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

import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.StreamSupport;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.script.AdaptedScriptEngine;
import org.scijava.script.ScriptModule;
import org.scijava.script.process.ScriptCallback;

/**
 * Scala interpreter
 *
 * @author Keith Schulze
 * @see ScriptEngine
 */
public class ScalaScriptEngine extends AdaptedScriptEngine {

    @Parameter
    private LogService log;

    public ScalaScriptEngine(ScriptEngine engine) {
        super(engine);
    }

    @Override
    public void put(final String key, final Object value) {
        // Need to get the ScriptModule instance in order to get the inputs
        // discovered at runtime. ScriptModule adds a reference to itself to
        // the Bindings at the start of its run method.
        Optional<String> outKey =
            Optional.ofNullable((ScriptModule) super.get(ScriptModule.class.getName()))
                .flatMap(sm -> {
                    Spliterator<ModuleItem<?>> inputs = sm.getInfo().inputs().spliterator();
                    Optional<String> output = StreamSupport.stream(inputs, false)
                        .filter(i -> i.getName() == key)
                        .findFirst()
                        .map((ModuleItem<?> i) -> {     // Foreach input param, cast to the known type
                            String name = i.getName();
                            String type = i.getType().getName();
                            String script = String.format("val %s: %s = _%s.asInstanceOf[%s]",
                                name, type, name, type);
                            ScriptCallback e = new ScriptCallback() {
                                @Override
                                public void invoke(ScriptModule module) throws ScriptException {
                                    module.getEngine().eval(script);
                                }
                            };
                            sm.getInfo().callbacks().add(e);
                            return ("_" + key);
                        });
                    return (output);
                });

        super.put(outKey.orElse(key), value);
    }

    @Override
    public Object get(String key) {
        // Values (variables) initialised or computed in the script are not added to
        // the bindings of the ScriptContext. One way to extract them is to evaluate
        // the variable and capture the return. So, first try to get value from bindings.
        // If that returns null, then evaluate the variable in the ScriptEngine. If this
        // returns null or throws a ScriptException, we simply return null.
        Optional<Object> result = Optional.ofNullable(super.get(key));

        return result.orElseGet(() -> {
            try {
                return super.eval(key);
            } catch (ScriptException se) {
                log.error(se.getMessage());
                return null;
            }
        });
    }
}
