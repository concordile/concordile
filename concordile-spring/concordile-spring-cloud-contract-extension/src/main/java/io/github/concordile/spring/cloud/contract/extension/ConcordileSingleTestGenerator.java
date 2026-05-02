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

package io.github.concordile.spring.cloud.contract.extension;

import io.github.concordile.spring.cloud.contract.api.ProducerContractContext;
import org.jspecify.annotations.Nullable;
import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator;
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ConcordileSingleTestGenerator implements SingleTestGenerator {

    private final SingleTestGenerator delegate = new JavaTestGenerator();

    private final JsonMapper jsonMapper = new JsonMapper();

    @Override
    public String buildClass(
            ContractVerifierConfigProperties properties,
            Collection<ContractMetadata> listOfFiles,
            String includedDirectoryRelativePath,
            GeneratedClassData generatedClassData
    ) {
        writeContext(listOfFiles, includedDirectoryRelativePath, generatedClassData);

        return delegate.buildClass(
                properties,
                listOfFiles,
                includedDirectoryRelativePath,
                generatedClassData
        );
    }

    private void writeContext(
            Collection<ContractMetadata> listOfFiles,
            @Nullable String includedDirectoryRelativePath,
            GeneratedClassData generatedClassData
    ) {
        var context = createContext(listOfFiles, includedDirectoryRelativePath, generatedClassData);
        var path = resolveContextPath(generatedClassData);

        try {
            var parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), context);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write Concordile producer context", exception);
        }
    }

    private Path resolveContextPath(GeneratedClassData generatedClassData) {
        var testClassPath = generatedClassData.testClassPath;
        var fileName = testClassPath.getFileName().toString();

        var contextFileName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.')) + ".concordile.json"
                : fileName + ".concordile.json";

        return testClassPath.resolveSibling(contextFileName);
    }

    private ProducerContractContext createContext(
            Collection<ContractMetadata> listOfFiles,
            @Nullable String includedDirectoryRelativePath,
            GeneratedClassData generatedClassData
    ) {
        var files = new ArrayList<ProducerContractContext.ContractFile>();

        for (var metadata : listOfFiles) {
            var contracts = createContracts(metadata);

            if (contracts.isEmpty()) {
                continue;
            }

            files.add(new ProducerContractContext.ContractFile(
                    resolveConsumerName(includedDirectoryRelativePath, metadata.getPath()),
                    resolveContractPath(metadata.getPath()),
                    resolveTestClassName(generatedClassData),
                    contracts
            ));
        }

        return new ProducerContractContext(files);
    }

    private List<ProducerContractContext.Contract> createContracts(ContractMetadata metadata) {
        var contracts = new ArrayList<ProducerContractContext.Contract>();

        for (SingleContractMetadata singleMetadata : metadata.getConvertedContractWithMetadata()) {
            if (singleMetadata.isIgnored()) {
                continue;
            }

            contracts.add(new ProducerContractContext.Contract(
                    resolveContractName(singleMetadata),
                    resolveTestMethodName(singleMetadata)
            ));
        }

        return contracts;
    }

    private String resolveContractName(SingleContractMetadata metadata) {
        var contractName = metadata.getContract().getName();

        if (contractName != null && !contractName.isBlank()) {
            return contractName;
        }

        return metadata.methodName();
    }

    private String resolveContractPath(Path path) {
        var normalizedPath = path.toString().replace('\\', '/');
        var marker = "/contracts/";
        var markerIndex = normalizedPath.indexOf(marker);

        if (markerIndex != -1) {
            return normalizedPath.substring(markerIndex + marker.length());
        }

        return normalizedPath;
    }

    private String resolveTestClassName(GeneratedClassData generatedClassData) {
        var className = resolveClassNameFromPath(generatedClassData.testClassPath);

        if (generatedClassData.classPackage == null || generatedClassData.classPackage.isBlank()) {
            return className;
        }

        return generatedClassData.classPackage + "." + className;
    }

    private String resolveClassNameFromPath(Path testClassPath) {
        var fileName = testClassPath.getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private String resolveTestMethodName(SingleContractMetadata metadata) {
        return "validate_" + metadata.methodName();
    }

    private String resolveConsumerName(@Nullable String includedDirectoryRelativePath, Path path) {
        if (includedDirectoryRelativePath != null && !includedDirectoryRelativePath.isBlank()) {
            return firstPathPart(includedDirectoryRelativePath);
        }

        var normalizedPath = path.toString().replace('\\', '/');
        var parts = normalizedPath.split("/");

        for (int index = 0; index < parts.length - 1; index++) {
            if ("contracts".equals(parts[index])) {
                return parts[index + 1];
            }
        }

        var parent = path.getParent();

        if (parent == null || parent.getFileName() == null) {
            throw new IllegalStateException("Cannot resolve consumer name from contract path: " + path);
        }

        return parent.getFileName().toString();
    }

    private String firstPathPart(String path) {
        var parts = path.replace('\\', '/').split("/");

        if (parts.length == 0 || parts[0].isBlank()) {
            throw new IllegalStateException("Cannot resolve consumer name from path: " + path);
        }

        return parts[0];
    }

}
