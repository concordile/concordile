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

package io.github.concordile.spring;

import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator;
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

import java.util.Collection;

public class ConcordileSingleTestGenerator implements SingleTestGenerator {

    private final SingleTestGenerator delegate;

    public ConcordileSingleTestGenerator() {
        this(new JavaTestGenerator());
    }

    public ConcordileSingleTestGenerator(SingleTestGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public String buildClass(
            ContractVerifierConfigProperties properties,
            Collection<ContractMetadata> listOfFiles,
            String includedDirectoryRelativePath,
            GeneratedClassData generatedClassData
    ) {
        return delegate.buildClass(properties, listOfFiles, includedDirectoryRelativePath, generatedClassData);
    }

}
