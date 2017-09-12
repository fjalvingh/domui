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

package com.vaadin.sass.testcases.scss;

import com.vaadin.sass.internal.*;
import com.vaadin.sass.internal.handler.*;
import com.vaadin.sass.testcases.scss.SassTestRunner.*;
import org.apache.commons.io.*;
import org.junit.*;
import org.w3c.css.sac.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDirectoryScanningSassTests {

    public static Collection<String> getScssResourceNames(URL directoryUrl)
            throws URISyntaxException, IOException {
        List<String> resources = new ArrayList<String>();
        for (String scssFile : getScssFiles(directoryUrl)) {
            resources.add(scssFile);
        }
        return resources;
    }

    private static List<String> getScssFiles(URL directoryUrl)
            throws URISyntaxException, IOException {
        URL sasslangUrl = directoryUrl;
        File sasslangDir = new File(sasslangUrl.toURI());
        File scssDir = new File(sasslangDir, "scss");
        Assert.assertTrue(scssDir.exists());

        List<File> scssFiles = new ArrayList<File>();
        addScssFilesRecursively(scssDir, scssFiles);

        List<String> scssRelativeNames = new ArrayList<String>();
        for (File f : scssFiles) {
            String relativeName = f.getCanonicalPath().substring(
                    scssDir.getCanonicalPath().length() + 1);
            scssRelativeNames.add(relativeName);
        }
        return scssRelativeNames;
    }

    private static void addScssFilesRecursively(File scssDir,
            List<File> scssFiles) {
        for (File f : scssDir.listFiles()) {
            if (f.isDirectory()) {
                addScssFilesRecursively(f, scssFiles);
            } else if (f.getName().endsWith(".scss")
                    && !f.getName().startsWith("_")) {
                scssFiles.add(f);
            }
        }
    }

    protected abstract URL getResourceURL(String path);

    @FactoryTest
    public void compareScssWithCss(String scssResourceName) throws Exception {
        File scssFile = getSassLangResourceFile(scssResourceName);

        SCSSDocumentHandler documentHandler = new SCSSDocumentHandlerImpl();
        SCSSErrorHandler errorHandler = new SCSSErrorHandler() {
            @Override
            public void error(CSSParseException arg0) throws CSSException {
                System.err.println(arg0.getMessage());
                //arg0.printStackTrace();
                super.error(arg0);
                Assert.fail(arg0.getMessage());
            }

            @Override
            public void fatalError(CSSParseException arg0) throws CSSException {
                //arg0.printStackTrace();
                super.error(arg0);
                Assert.fail(arg0.getMessage());
            }

            @Override
            public void traverseError(Exception e) {
                //e.printStackTrace();
                super.traverseError(e);
                Assert.fail(e.getMessage());
            }

            @Override
            public void traverseError(String message) {
                super.traverseError(message);
                Assert.fail(message);
            }

            @Override public void warning(CSSParseException e) throws CSSException {
                warn("Warning when parsing file \n" + e.getURI() + " on line "
                    + e.getLineNumber() + ", column " + e.getColumnNumber());
            }

            private void warn(String msg) {
                Logger.getLogger(SCSSDocumentHandlerImpl.class.getName()).log(
                    Level.WARNING, msg);
            }
        };

        ScssStylesheet scssStylesheet = ScssStylesheet.get(
                scssFile.getCanonicalPath(), null, documentHandler,
                errorHandler);
        scssStylesheet.compile();
        String parsedCss = scssStylesheet.printState();

        if (getCssFile(scssFile) != null) {
            String referenceCss = IOUtils.toString(new FileInputStream(
                    getCssFile(scssFile)));
            String normalizedReference = normalize(referenceCss);
            String normalizedParsed = normalize(parsedCss);

            Assert.assertEquals("Original CSS and parsed CSS do not match for "
                    + scssResourceName, normalizedReference, normalizedParsed);
        }
    }

    private String normalize(String css) {
        // one space after comma, no whitespace before comma
        css = css.replaceAll("[\n\r\t ]*,[\n\r\t ]*", ", ");
        // add whitespace before opening brace
        css = css.replaceAll("[\n\r\t ]*\\{", " {");
        // remove whitespace after opening parenthesis and before closing
        // parenthesis
        css = css.replaceAll("\\([\n\r\t ]*", "(");
        css = css.replaceAll("[\n\r\t ]*\\)", ")");
        // Replace multiple whitespace characters with a single space to compact
        css = css.replaceAll("[\n\r\t ]+", " ");
        // remove trailing whitespace
        css = css.replaceAll("[\n\r\t ]*$", "");
        css = css.replaceAll("[\n\r\t ]*;", ";\n");
        css = css.replaceAll("\\{", "\\{\n");
        css = css.replaceAll("[\n\r\t ]*}", "}\n");
        // remove initial whitespace
        css = css.replaceAll("^[\t ]*", "");
        return css;
    }

    private File getSassLangResourceFile(String resourceName)
            throws IOException, URISyntaxException {
        String base = "/scss/";
        String fullResourceName = base + resourceName;
        URL res = getResourceURL(fullResourceName);
        if (res == null) {
            throw new FileNotFoundException("Resource " + resourceName
                    + " not found (tried " + fullResourceName + ")");
        }
        return new File(res.toURI());
    }

    protected File getCssFile(File scssFile) throws IOException {
        return new File(scssFile.getCanonicalPath().replace("scss", "css"));
    }
}
