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

package io.github.concordile.spring.cloud.contract.consumer;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConcordileConsumerStubUsageListener extends AbstractTestExecutionListener {

    private static final String OUTPUT_DIRECTORY_PROPERTY = "concordile.consumer.stub-usage-dir";

    private final JsonMapper jsonMapper = new JsonMapper();

    @Override
    public void beforeTestExecution(TestContext testContext) {
        ConsumerStubUsageRegistry.begin(
                testContext.getTestClass().getName(),
                testContext.getTestMethod().getName()
        );
    }

    @Override
    public void afterTestExecution(TestContext testContext) throws Exception {
        try {
            var context = ConsumerStubUsageRegistry.snapshot();

            if (context == null || context.matchedMappingIds().isEmpty()) {
                return;
            }

            var outputDirectory = System.getProperty(OUTPUT_DIRECTORY_PROPERTY);

            if (outputDirectory == null || outputDirectory.isBlank()) {
                return;
            }

            var outputPath = Path.of(
                    outputDirectory,
                    context.testClassName().replace('.', '-')
                            + "-"
                            + context.testMethodName()
                            + ".json"
            );

            Files.createDirectories(outputPath.getParent());

            jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(outputPath.toFile(), context);
        } finally {
            ConsumerStubUsageRegistry.end();
        }
    }

}
