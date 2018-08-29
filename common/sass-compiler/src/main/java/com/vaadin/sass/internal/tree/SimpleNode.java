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
package com.vaadin.sass.internal.tree;

import java.util.Collection;
import java.util.Collections;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.util.StringUtil;

/**
 * A simple BlockNode where input text equals output. <b>Note : </b> ignores any
 * possible children so only use it when you are sure no child nodes will be
 * applied.
 * 
 * @author Sebastian Nyholm @ Vaadin Ltd
 * 
 */
public class SimpleNode extends Node implements IVariableNode {

    private String text;

    public SimpleNode(String text) {
        this.text = text;
    }

    private SimpleNode(SimpleNode nodeToCopy) {
        super(nodeToCopy);
        text = nodeToCopy.text;
    }

    @Override
    public String printState() {
        return text;
    }

    @Override
    public String toString() {
        return printState();
    }

    @Override
    public void replaceVariables(ScssContext context) {
        for (final Variable var : context.getVariables()) {
            if (StringUtil.containsVariable(text, var.getName())) {
                text = StringUtil.replaceVariable(text, var.getName(), var
                        .getExpr().printState());
            }
        }
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        replaceVariables(context);
        return Collections.singleton((Node) this);
    }

    @Override
    public SimpleNode copy() {
        return new SimpleNode(this);
    }
}
