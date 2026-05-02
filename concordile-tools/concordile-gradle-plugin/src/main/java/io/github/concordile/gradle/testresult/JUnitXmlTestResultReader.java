/*
 * Copyright 2025-present The Concordile Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.concordile.gradle.testresult;

import io.github.concordile.broker.api.v1.VerificationStatus;
import org.gradle.api.GradleException;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class JUnitXmlTestResultReader {

    public Map<String, VerificationStatus> read(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            throw new GradleException("Test results directory does not exist: " + directory);
        }

        var results = new HashMap<String, VerificationStatus>();

        try (var files = Files.walk(directory)) {
            var xmlFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .toList();

            for (var file : xmlFiles) {
                readFile(file, results);
            }
        }

        return results;
    }

    private void readFile(
            Path file,
            Map<String, VerificationStatus> results
    ) throws Exception {
        var documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        var document = documentBuilderFactory.newDocumentBuilder().parse(file.toFile());
        var testCases = document.getElementsByTagName("testcase");

        for (int index = 0; index < testCases.getLength(); index++) {
            var testCase = (Element) testCases.item(index);

            var className = testCase.getAttribute("classname");
            var methodName = normalizeMethodName(testCase.getAttribute("name"));

            if (className.isBlank() || methodName.isBlank()) {
                continue;
            }

            var failed = testCase.getElementsByTagName("failure").getLength() > 0
                    || testCase.getElementsByTagName("error").getLength() > 0;

            results.put(
                    className + "#" + methodName,
                    failed ? VerificationStatus.FAILED : VerificationStatus.PASSED
            );
        }
    }

    private String normalizeMethodName(String methodName) {
        if (methodName.endsWith("()")) {
            return methodName.substring(0, methodName.length() - 2);
        }

        return methodName;
    }

}
