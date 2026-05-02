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

package io.github.concordile.gradle.model;

import io.github.concordile.broker.api.v1.VerificationPartyRole;

import java.util.List;

public record ConsumerVerificationContext(
        VerificationPartyRole role,
        Application application,
        String version,
        List<Counterparty> counterparties
) {

    public record Application(
            String groupId,
            String name
    ) {
    }

    public record Counterparty(
            Application application,
            String version,
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
            String testClassName,
            String testMethodName
    ) {
    }

}
