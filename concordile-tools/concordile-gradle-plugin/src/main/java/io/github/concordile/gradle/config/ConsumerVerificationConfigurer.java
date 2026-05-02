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

import io.github.concordile.gradle.extension.ConcordileExtension;
import io.github.concordile.gradle.task.PrepareConsumerVerificationContextTask;
import io.github.concordile.gradle.task.PublishConsumerVerificationTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.concordile.gradle.ConcordilePluginConstants.CONSUMER_VERIFICATION_CONTEXT_FILE;
import static io.github.concordile.gradle.ConcordilePluginConstants.GROUP_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.PREPARE_CONSUMER_VERIFICATION_CONTEXT_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.PUBLISH_CONSUMER_VERIFICATION_TASK_NAME;
import static io.github.concordile.gradle.ConcordilePluginConstants.SPRING_CLOUD_CONTRACT_CONSUMER_EXTENSION_GAV;
import static io.github.concordile.gradle.ConcordilePluginConstants.TEST_TASK_NAME;

public final class ConsumerVerificationConfigurer {

    private static final String TEST_RESULTS_DIR = "test-results/test";

    private static final String CONSUMER_STUB_USAGE_DIR = "concordile/consumer-stub-usage";

    private static final String TEST_IMPLEMENTATION_CONFIGURATION_NAME = "testImplementation";

    private static final String TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "testRuntimeClasspath";

    private final Project project;

    private final ConcordileExtension extension;

    public ConsumerVerificationConfigurer(
            Project project,
            ConcordileExtension extension
    ) {
        this.project = project;
        this.extension = extension;
    }

    public void configure() {
        configureTestTasks();
        configureDependencies();

        var consumerContextFile = project.getLayout()
                .getBuildDirectory()
                .file(CONSUMER_VERIFICATION_CONTEXT_FILE);

        var prepareConsumerContext = registerPrepareConsumerContextTask(consumerContextFile);

        registerPublishConsumerVerificationTask(
                consumerContextFile,
                prepareConsumerContext
        );
    }

    private void configureTestTasks() {
        var stubUsageDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(CONSUMER_STUB_USAGE_DIR);

        project.getTasks().withType(Test.class).configureEach(test -> {
            test.getOutputs().dir(stubUsageDirectory);

            test.doFirst(task -> {
                var outputDirectory = stubUsageDirectory.get().getAsFile();

                project.delete(outputDirectory);

                test.systemProperty(
                        "concordile.consumer.stub-usage-dir",
                        outputDirectory.getAbsolutePath()
                );
            });
        });
    }

    private void configureDependencies() {
        project.getPluginManager().withPlugin("java", ignored -> {
            var added = new AtomicBoolean(false);

            for (var configurationName : List.of("testImplementation", "testRuntimeOnly")) {
                project.getConfigurations().named(configurationName, configuration ->
                        configuration.getDependencies().all(dependency -> {
                            if (added.get()) {
                                return;
                            }

                            if (!isSpringCloudContractStubRunnerDependency(dependency)) {
                                return;
                            }

                            project.getDependencies().add(
                                    TEST_IMPLEMENTATION_CONFIGURATION_NAME,
                                    SPRING_CLOUD_CONTRACT_CONSUMER_EXTENSION_GAV
                            );

                            added.set(true);
                        })
                );
            }
        });
    }

    private boolean isSpringCloudContractStubRunnerDependency(Dependency dependency) {
        return "org.springframework.cloud".equals(dependency.getGroup())
                && (
                "spring-cloud-starter-contract-stub-runner".equals(dependency.getName())
                        || "spring-cloud-contract-stub-runner".equals(dependency.getName())
        );
    }

    private TaskProvider<PrepareConsumerVerificationContextTask> registerPrepareConsumerContextTask(
            Provider<RegularFile> consumerContextFile
    ) {
        var stubUsageFiles = project.getLayout()
                .getBuildDirectory()
                .dir(CONSUMER_STUB_USAGE_DIR)
                .map(directory -> project.fileTree(directory.getAsFile(), fileTree ->
                        fileTree.include("**/*.json")
                ));

        return project.getTasks().register(
                PREPARE_CONSUMER_VERIFICATION_CONTEXT_TASK_NAME,
                PrepareConsumerVerificationContextTask.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Prepares Concordile consumer verification context from used SCC stubs.");

                    task.dependsOn(TEST_TASK_NAME);

                    task.getStubUsageFiles().from(stubUsageFiles);
                    task.getRuntimeClasspath().from(
                            project.getConfigurations().named(TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                    );

                    task.getContextFile().set(consumerContextFile);

                    task.getApplicationGroupId().set(extension.getApplication().getGroupId());
                    task.getApplicationName().set(extension.getApplication().getName());
                    task.getApplicationVersion().set(extension.getApplication().getVersion());
                }
        );
    }

    private TaskProvider<PublishConsumerVerificationTask> registerPublishConsumerVerificationTask(
            Provider<RegularFile> consumerContextFile,
            TaskProvider<PrepareConsumerVerificationContextTask> prepareConsumerContext
    ) {
        return project.getTasks().register(
                PUBLISH_CONSUMER_VERIFICATION_TASK_NAME,
                PublishConsumerVerificationTask.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Publishes Concordile consumer verification results.");

                    task.dependsOn(prepareConsumerContext);
                    task.getContextFile().set(consumerContextFile);
                    task.getBrokerUrl().set(extension.getBroker().getUrl());
                    task.getTestResultsDirectory().set(
                            project.getLayout().getBuildDirectory().dir(TEST_RESULTS_DIR)
                    );
                }
        );
    }

}
