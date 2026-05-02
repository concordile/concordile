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
import io.github.concordile.gradle.model.ConsumerVerificationContext;
import io.github.concordile.spring.cloud.contract.api.ConsumerStubUsageContext;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class PrepareConsumerVerificationContextTask extends DefaultTask {

    private final JsonMapper jsonMapper = new JsonMapper();

    @InputFiles
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getStubUsageFiles();

    @Classpath
    public abstract ConfigurableFileCollection getRuntimeClasspath();

    @OutputFile
    public abstract RegularFileProperty getContextFile();

    @Input
    public abstract Property<String> getApplicationGroupId();

    @Input
    public abstract Property<String> getApplicationName();

    @Input
    public abstract Property<String> getApplicationVersion();

    @TaskAction
    public void prepare() {
        var usageFiles = getStubUsageFiles().getFiles().stream()
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .toList();

        if (usageFiles.isEmpty()) {
            throw new GradleException("""
                    Concordile consumer stub usage files were not found.
                    
                    Expected files:
                    build/concordile/consumer-stub-usage/*.json
                    
                    Make sure:
                    1. Consumer tests were executed.
                    2. ConcordileConsumerStubUsageListener is on the test classpath.
                    3. The test uses Spring Cloud Contract Stub Runner.
                    """);
        }

        var mappingsById = readStubMappingsById();

        if (mappingsById.isEmpty()) {
            throw new GradleException("""
                    Cannot find WireMock mappings in test runtime classpath.
                    
                    Expected producer stubs jar on testRuntimeClasspath, for example:
                    producer-service-0.1.0-SNAPSHOT-stubs.jar
                    """);
        }

        var counterparties = new LinkedHashMap<String, MutableCounterparty>();

        for (var usageFile : usageFiles) {
            var usage = jsonMapper.readValue(usageFile, ConsumerStubUsageContext.class);

            for (var mappingId : usage.matchedMappingIds()) {
                var mapping = mappingsById.get(mappingId);

                if (mapping == null) {
                    throw new GradleException(
                            "Cannot find WireMock mapping '%s' in producer stubs jars".formatted(mappingId)
                    );
                }

                var counterpartyKey = mapping.groupId()
                        + ":"
                        + mapping.artifactId()
                        + ":"
                        + mapping.version();

                var counterparty = counterparties.computeIfAbsent(
                        counterpartyKey,
                        ignored -> new MutableCounterparty(
                                new ConsumerVerificationContext.Application(
                                        mapping.groupId(),
                                        mapping.artifactId()
                                ),
                                mapping.version()
                        )
                );

                var contractFile = counterparty.files.computeIfAbsent(
                        mapping.contractPath(),
                        ignored -> new MutableContractFile(mapping.contractPath())
                );

                var contractKey = mapping.contractName()
                        + "#"
                        + usage.testClassName()
                        + "#"
                        + usage.testMethodName();

                contractFile.contracts.putIfAbsent(
                        contractKey,
                        new ConsumerVerificationContext.Contract(
                                mapping.contractName(),
                                usage.testClassName(),
                                usage.testMethodName()
                        )
                );
            }
        }

        if (counterparties.isEmpty()) {
            throw new GradleException("""
                    No matched SCC stub mappings were found.
                    
                    The consumer tests ran, but no matched WireMock mapping IDs were collected.
                    """);
        }

        var context = new ConsumerVerificationContext(
                VerificationPartyRole.CONSUMER,
                new ConsumerVerificationContext.Application(
                        getApplicationGroupId().get(),
                        getApplicationName().get()
                ),
                getApplicationVersion().get(),
                counterparties.values().stream()
                        .map(MutableCounterparty::toContext)
                        .toList()
        );

        var outputFile = getContextFile().get().getAsFile();
        outputFile.getParentFile().mkdirs();

        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, context);
    }

    private Map<String, StubMappingMetadata> readStubMappingsById() {
        var mappingsById = new LinkedHashMap<String, StubMappingMetadata>();

        for (var file : getRuntimeClasspath().getFiles()) {
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }

            try {
                readStubMappingsFromJar(file, mappingsById);
            } catch (Exception exception) {
                getLogger().debug("Cannot read stubs jar " + file, exception);
            }
        }

        return mappingsById;
    }

    private void readStubMappingsFromJar(
            File jarFile,
            Map<String, StubMappingMetadata> mappingsById
    ) throws Exception {
        try (var zipFile = new ZipFile(jarFile)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                var path = entry.getName();

                if (!path.endsWith(".json") || !path.contains("/mappings/")) {
                    continue;
                }

                var metadata = readMappingMetadata(zipFile, entry);

                if (metadata != null) {
                    mappingsById.put(metadata.mappingId(), metadata);
                }
            }
        }
    }

    private @Nullable StubMappingMetadata readMappingMetadata(
            ZipFile zipFile,
            ZipEntry entry
    ) throws Exception {
        var path = entry.getName();
        var parts = path.split("/");

        if (parts.length < 6) {
            return null;
        }

        if (!"META-INF".equals(parts[0])) {
            return null;
        }

        if (!"mappings".equals(parts[4])) {
            return null;
        }

        var groupId = parts[1];
        var artifactId = parts[2];
        var version = parts[3];

        try (var inputStream = zipFile.getInputStream(entry)) {
            var json = jsonMapper.readValue(inputStream, Map.class);
            var id = json.get("id");

            if (!(id instanceof String mappingId) || mappingId.isBlank()) {
                return null;
            }

            var mappingsPrefix = "META-INF/"
                    + groupId
                    + "/"
                    + artifactId
                    + "/"
                    + version
                    + "/mappings/";

            var mappingRelativePath = path.substring(mappingsPrefix.length());
            var contractPath = resolveContractPath(
                    zipFile,
                    groupId,
                    artifactId,
                    version,
                    mappingRelativePath
            );

            return new StubMappingMetadata(
                    mappingId,
                    groupId,
                    artifactId,
                    version,
                    contractPath,
                    resolveContractName(contractPath)
            );
        }
    }

    private String resolveContractPath(
            ZipFile zipFile,
            String groupId,
            String artifactId,
            String version,
            String mappingRelativePath
    ) {
        var contractsPrefix = "META-INF/"
                + groupId
                + "/"
                + artifactId
                + "/"
                + version
                + "/contracts/";

        var basePath = removeExtension(mappingRelativePath);

        for (var extension : List.of(".groovy", ".yml", ".yaml", ".json")) {
            var candidate = contractsPrefix + basePath + extension;

            if (zipFile.getEntry(candidate) != null) {
                return basePath + extension;
            }
        }

        return mappingRelativePath;
    }

    private String resolveContractName(String contractPath) {
        var fileName = contractPath;

        var slashIndex = fileName.lastIndexOf('/');

        if (slashIndex != -1) {
            fileName = fileName.substring(slashIndex + 1);
        }

        return removeExtension(fileName);
    }

    private String removeExtension(String value) {
        var dotIndex = value.lastIndexOf('.');

        if (dotIndex == -1) {
            return value;
        }

        return value.substring(0, dotIndex);
    }

    private record StubMappingMetadata(
            String mappingId,
            String groupId,
            String artifactId,
            String version,
            String contractPath,
            String contractName
    ) {
    }

    private static final class MutableCounterparty {

        private final ConsumerVerificationContext.Application application;

        private final String version;

        private final Map<String, MutableContractFile> files = new LinkedHashMap<>();

        private MutableCounterparty(
                ConsumerVerificationContext.Application application,
                String version
        ) {
            this.application = application;
            this.version = version;
        }

        private ConsumerVerificationContext.Counterparty toContext() {
            return new ConsumerVerificationContext.Counterparty(
                    application,
                    version,
                    files.values().stream()
                            .map(MutableContractFile::toContext)
                            .toList()
            );
        }

    }

    private static final class MutableContractFile {

        private final String path;

        private final Map<String, ConsumerVerificationContext.Contract> contracts = new LinkedHashMap<>();

        private MutableContractFile(String path) {
            this.path = path;
        }

        private ConsumerVerificationContext.ContractFile toContext() {
            return new ConsumerVerificationContext.ContractFile(
                    path,
                    new ArrayList<>(contracts.values())
            );
        }

    }

}
