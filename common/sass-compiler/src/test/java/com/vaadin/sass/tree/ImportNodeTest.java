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

package com.vaadin.sass.tree;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.css.sac.SACMediaList;

import com.vaadin.sass.internal.tree.ImportNode;

public class ImportNodeTest {
    private static class SingleNodeMediaList implements SACMediaList {

        private String item;

        public SingleNodeMediaList(String item) {
            this.item = item;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public String item(int index) {
            return (index == 0) ? item : null;
        }

    }

    @Test
    public void testIsPureCssImportShouldReturnTrueWhenIsURL() {
        ImportNode node = new ImportNode("", null, true);
        Assert.assertTrue(node.isPureCssImport());
    }

    @Test
    public void testIsPureCssImportShouldReturnTrueWhenStartsWithHttp() {
        ImportNode node = new ImportNode("http://abc", null, false);
        Assert.assertTrue(node.isPureCssImport());
    }

    @Test
    public void testIsPureCssImportShouldReturnTrueWhenEndsWithCss() {
        ImportNode node = new ImportNode("abc.css", null, false);
        Assert.assertTrue(node.isPureCssImport());
    }

    @Test
    public void testIsPureCssImportShouldReturnTrueWhenHasMediaQueries() {
        SACMediaList ml = new SingleNodeMediaList("screen");
        ImportNode node = new ImportNode("", ml, false);
        Assert.assertTrue(node.isPureCssImport());
    }

    @Test
    public void testIsPureCssImportShouldReturnFalseInOtherCases() {
        ImportNode node = new ImportNode("", null, false);
        Assert.assertFalse(node.isPureCssImport());
    }

    @Test
    public void testSerializeWhenIsURL() {
        ImportNode node = new ImportNode("test", null, true);
        Assert.assertEquals("@import url(test);", node.printState());
    }

    @Test
    public void testSerializeWhenIsNotURL() {
        ImportNode node = new ImportNode("test", null, false);
        Assert.assertEquals("@import \"test\";", node.printState());
    }

    @Test
    public void testSerializeWithMediaQueries() {
        SACMediaList ml = new SingleNodeMediaList("screen");
        ImportNode node = new ImportNode("test", ml, true);
        Assert.assertEquals("@import url(test) screen;", node.printState());
    }

    @Test
    public void testUpdateURL() {
        ImportNode node = new ImportNode("bar.css", null, true);
        node = node.updateUrl("foo/");
        Assert.assertEquals("@import url(foo/bar.css);", node.printState());
    }
}
