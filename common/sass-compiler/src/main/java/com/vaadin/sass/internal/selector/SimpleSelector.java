/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.sass.internal.selector;

import java.io.Serializable;

import com.vaadin.sass.internal.ScssContext;

/**
 * Simple CSS3 selector such as an id selector or an attribute selector.
 * 
 * {@link SimpleSelector} instances are immutable and
 * {@link #replaceVariables()} returns a modified copy of the selector if
 * necessary.
 * 
 * Multiple concatenated simple selectors are grouped in
 * {@link SimpleSelectorSequence} and multiple sequences of selectors joined
 * with combinators are then joined into {@link Selector}.
 */
public abstract class SimpleSelector implements Serializable {

    public SimpleSelector replaceVariables(ScssContext context) {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass())
                && toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
