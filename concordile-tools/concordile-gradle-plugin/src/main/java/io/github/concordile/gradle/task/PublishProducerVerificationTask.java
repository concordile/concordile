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

package io.github.concordile.gradle.task;

import io.github.concordile.gradle.client.ConcordileClient;
import io.github.concordile.gradle.context.VerificationContextFactory;
import io.github.concordile.gradle.mapper.ProducerVerificationRequestMapper;
import io.github.concordile.gradle.model.ProducerVerificationContext;
import io.github.concordile.gradle.testresult.JUnitXmlTestResultReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import tools.jackson.databind.json.JsonMapper;

public abstract class PublishProducerVerificationTask extends DefaultTask {

    private final JsonMapper jsonMapper = new JsonMapper();

    private final JUnitXmlTestResultReader testResultReader = new JUnitXmlTestResultReader();

    private final ProducerVerificationRequestMapper requestMapper = new ProducerVerificationRequestMapper();

    private final VerificationContextFactory contextFactory = new VerificationContextFactory();

    private final ConcordileClient client = new ConcordileClient();

    @InputFile
    public abstract RegularFileProperty getContextFile();

    @InputDirectory
    public abstract DirectoryProperty getTestResultsDirectory();

    @Input
    public abstract Property<String> getBrokerUrl();

    @TaskAction
    public void publish() throws Exception {
        var context = jsonMapper.readValue(
                getContextFile().get().getAsFile(),
                ProducerVerificationContext.class
        );

        var testResults = testResultReader.read(
                getTestResultsDirectory().get().getAsFile().toPath()
        );

        var request = requestMapper.map(
                context,
                testResults,
                contextFactory.create(getProject())
        );

        client.createVerification(
                getBrokerUrl().get(),
                request
        );
    }

}
