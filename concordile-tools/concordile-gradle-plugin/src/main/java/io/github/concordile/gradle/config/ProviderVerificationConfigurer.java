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

package io.github.concordile.gradle.config;

import io.github.concordile.gradle.extension.ConcordileConsumerExtension;
import io.github.concordile.gradle.extension.ConcordileExtension;
import io.github.concordile.gradle.task.PrepareProviderVerificationContextTask;
import io.github.concordile.gradle.task.PublishProviderVerificationTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.util.stream.Collectors;

import static io.github.concordile.gradle.ConcordilePluginConstants.CONTRACT_TEST_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.GENERATE_CONTRACT_TESTS_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.GROUP_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.PREPARE_PROVIDER_VERIFICATION_CONTEXT_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.PROVIDER_VERIFICATION_CONTEXT_FILE;
import static io.github.concordile.gradle.ConcordilePluginConstants.PUBLISH_PROVIDER_VERIFICATION_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.SPRING_CLOUD_CONTRACT_PLUGIN_ID;
import static io.github.concordile.gradle.ConcordilePluginConstants.SPRING_CLOUD_CONTRACT_PROVIDER_EXTENSION_GAV;

public final class ProviderVerificationConfigurer {

    private static final String GENERATED_CONTRACT_TESTS_DIR = "generated-test-sources";
    private static final String CONCORDILE_CONTEXT_FILE_PATTERN = "**/*.concordile.json";
    private static final String CONTRACT_TEST_RESULTS_DIR = "test-results/contractTest";

    private final Project project;

    private final ConcordileExtension extension;

    public ProviderVerificationConfigurer(
            Project project,
            ConcordileExtension extension
    ) {
        this.project = project;
        this.extension = extension;
    }

    public void configure() {
        var providerContextFile = project.getLayout()
                .getBuildDirectory()
                .file(PROVIDER_VERIFICATION_CONTEXT_FILE);

        var prepareProviderContext = registerPrepareProviderContextTask(providerContextFile);

        var publishProviderVerification = registerPublishProviderVerificationTask(
                providerContextFile,
                prepareProviderContext
        );

        configureProviderSpringCloudContractIntegration(
                prepareProviderContext,
                publishProviderVerification
        );
    }

    private TaskProvider<PrepareProviderVerificationContextTask> registerPrepareProviderContextTask(
            Provider<RegularFile> providerContextFile
    ) {
        var generatedContractTestsDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(GENERATED_CONTRACT_TESTS_DIR);

        var rawContextFiles = generatedContractTestsDirectory.map(directory ->
                project.fileTree(directory.getAsFile(), fileTree ->
                        fileTree.include(CONCORDILE_CONTEXT_FILE_PATTERN)
                )
        );

        return project.getTasks().register(
                PREPARE_PROVIDER_VERIFICATION_CONTEXT_TASK_NAME,
                PrepareProviderVerificationContextTask.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Prepares Concordile provider verification context from SCC contracts.");

                    task.getRawContextFiles().from(rawContextFiles);
                    task.getContextFile().set(providerContextFile);

                    task.getApplicationGroupId().set(extension.getApplication().getGroupId());
                    task.getApplicationName().set(extension.getApplication().getName());
                    task.getApplicationVersion().set(extension.getApplication().getVersion());

                    task.getConsumerGroupIds().set(project.provider(() ->
                            extension.getConsumers().stream()
                                    .collect(Collectors.toMap(
                                            ConcordileConsumerExtension::getName,
                                            consumer -> consumer.getGroupId().get()
                                    ))
                    ));
                }
        );
    }

    private TaskProvider<PublishProviderVerificationTask> registerPublishProviderVerificationTask(
            Provider<RegularFile> providerContextFile,
            TaskProvider<PrepareProviderVerificationContextTask> prepareProviderContext
    ) {
        return project.getTasks().register(
                PUBLISH_PROVIDER_VERIFICATION_TASK_NAME,
                PublishProviderVerificationTask.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Publishes Concordile provider verification results.");

                    task.dependsOn(prepareProviderContext);
                    task.getContextFile().set(providerContextFile);
                    task.getBrokerUrl().set(extension.getBroker().getUrl());
                    task.getTestResultsDirectory().set(
                            project.getLayout().getBuildDirectory().dir(CONTRACT_TEST_RESULTS_DIR)
                    );
                }
        );
    }

    private void configureProviderSpringCloudContractIntegration(
            TaskProvider<PrepareProviderVerificationContextTask> prepareProviderContext,
            TaskProvider<PublishProviderVerificationTask> publishProviderVerification
    ) {
        project.getPluginManager().withPlugin(SPRING_CLOUD_CONTRACT_PLUGIN_ID, plugin -> {
            project.getDependencies().add(
                    "contractTestImplementation",
                    SPRING_CLOUD_CONTRACT_PROVIDER_EXTENSION_GAV
            );

            prepareProviderContext.configure(task ->
                    task.dependsOn(GENERATE_CONTRACT_TESTS_TASK_NAME)
            );

            publishProviderVerification.configure(task ->
                    task.dependsOn(CONTRACT_TEST_TASK_NAME)
            );
        });
    }

}
