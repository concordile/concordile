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

package io.github.concordile.spring.cloud.contract.api;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ProducerContractContext(
        @Nullable List<ContractFile> files
) {

    public ProducerContractContext {
        files = files == null ? List.of() : List.copyOf(files);
    }

    public ProducerContractContext merge(ProducerContractContext other) {
        var mergedFiles = new ArrayList<ContractFile>();
        mergedFiles.addAll(files);
        mergedFiles.addAll(other.files());
        return new ProducerContractContext(mergedFiles);
    }

    public record ContractFile(
            String consumerName,
            String path,
            String testClassName,
            @Nullable List<Contract> contracts
    ) {

        public ContractFile {
            contracts = contracts == null ? List.of() : List.copyOf(contracts);
        }

    }

    public record Contract(
            String name,
            String testMethodName
    ) {
    }

}
