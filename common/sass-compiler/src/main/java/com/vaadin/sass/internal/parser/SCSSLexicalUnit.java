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
package com.vaadin.sass.internal.parser;

import org.w3c.css.sac.LexicalUnit;

public interface SCSSLexicalUnit extends LexicalUnit {
    static final short SCSS_VARIABLE = 100;
    static final short SCSS_OPERATOR_LEFT_PAREN = 101;
    static final short SCSS_OPERATOR_RIGHT_PAREN = 102;
    static final short SCSS_OPERATOR_EQUALS = 103;
    static final short SCSS_OPERATOR_NOT_EQUAL = 104;
    static final short SCSS_OPERATOR_AND = 105;
    static final short SCSS_OPERATOR_OR = 106;
    static final short SCSS_OPERATOR_NOT = 107;
    static final short SCSS_NULL = 110;

    static final short SAC_LEM = 200;
    static final short SAC_REM = 201;

}
