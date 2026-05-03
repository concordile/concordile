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

import io.github.concordile.broker.api.v1.VerificationPartyRole;
import io.github.concordile.gradle.model.ProviderVerificationContext;
import io.github.concordile.spring.cloud.contract.api.ProviderContractContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class PrepareProviderVerificationContextTask extends DefaultTask {

    private final JsonMapper jsonMapper = new JsonMapper();

    @InputFiles
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getRawContextFiles();

    @OutputFile
    public abstract RegularFileProperty getContextFile();

    @Input
    public abstract Property<String> getApplicationGroupId();

    @Input
    public abstract Property<String> getApplicationName();

    @Input
    public abstract Property<String> getApplicationVersion();

    @Input
    public abstract MapProperty<String, String> getConsumerGroupIds();

    @TaskAction
    public void prepare() {
        var rawContextFiles = getRawContextFiles().getFiles().stream()
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .toList();

        if (rawContextFiles.isEmpty()) {
            throw new GradleException("""
                    Concordile provider raw context files were not found.
                    
                    Expected files:
                    build/generated-test-sources/**/*.concordile.json
                    
                    Make sure:
                    1. The 'org.springframework.cloud.contract' plugin is applied.
                    2. The 'generateContractTests' task runs before this task.
                    3. ConcordileSingleTestGenerator is loaded by SCC.
                    4. There are provider contracts to generate.
                    """);
        }

        var rawContext = new ProviderContractContext(List.of());

        for (var file : rawContextFiles) {
            var nextContext = jsonMapper.readValue(file, ProviderContractContext.class);
            rawContext = rawContext.merge(nextContext);
        }

        var context = new ProviderVerificationContext(
                VerificationPartyRole.PROVIDER,
                new ProviderVerificationContext.Application(
                        getApplicationGroupId().get(),
                        getApplicationName().get()
                ),
                getApplicationVersion().get(),
                createCounterparties(rawContext)
        );

        var outputFile = getContextFile().get().getAsFile();
        outputFile.getParentFile().mkdirs();

        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, context);
    }

    private List<ProviderVerificationContext.Counterparty> createCounterparties(
            ProviderContractContext rawContext
    ) {
        var consumerGroupIds = getConsumerGroupIds().get();
        var filesByConsumer = new LinkedHashMap<String, List<ProviderVerificationContext.ContractFile>>();

        for (var rawFile : rawContext.files()) {
            var contracts = rawFile.contracts().stream()
                    .map(contract -> new ProviderVerificationContext.Contract(
                            contract.name(),
                            rawFile.testClassName(),
                            contract.testMethodName()
                    ))
                    .toList();

            filesByConsumer.computeIfAbsent(rawFile.consumerName(), ignored -> new ArrayList<>())
                    .add(new ProviderVerificationContext.ContractFile(rawFile.path(), contracts));
        }

        var counterparties = new ArrayList<ProviderVerificationContext.Counterparty>();

        for (var entry : filesByConsumer.entrySet()) {
            var consumerName = entry.getKey();
            var groupId = consumerGroupIds.get(consumerName);

            if (groupId == null || groupId.isBlank()) {
                throw new GradleException("""
                        Cannot resolve groupId for consumer '%s'.
                        
                        Add it to Gradle configuration:
                        
                        concordile {
                            consumers {
                                '%s' {
                                    groupId = "foo.bar"
                                }
                            }
                        }
                        """.formatted(consumerName, consumerName));
            }

            counterparties.add(new ProviderVerificationContext.Counterparty(
                    new ProviderVerificationContext.Application(groupId, consumerName),
                    entry.getValue()
            ));
        }

        return counterparties;
    }

}
