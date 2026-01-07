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

package io.github.concordile.broker.payload;

import java.util.List;

public record RegisterVerificationRequest(
        Producer producer,
        List<Consumer> consumers
) {

    public record Producer(
            String group,
            String name,
            String version
    ) {
    }

    public record Consumer(
            String name,
            List<ContractFile> files
    ) {
    }

    public record ContractFile(
            String path,
            List<Contract> contracts
    ) {
    }

    public record Contract(
            String name,
            String status
    ) {
    }

}
