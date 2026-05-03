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

package io.github.concordile.broker.api.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record CreateVerificationRequest(
        @NotNull @Valid Party party,
        @Nullable Map<String, Object> context,
        @NotEmpty List<@Valid Counterparty> counterparties
) {

    public record Party(
            @NotNull VerificationPartyRole role,
            @NotNull @Valid Application application,
            @NotBlank String version
    ) {
    }

    public record Counterparty(
            @NotNull @Valid Application application,
            @Nullable String version,
            @NotEmpty List<@Valid ContractFile> files
    ) {
    }

    public record Application(
            @NotBlank String groupId,
            @NotBlank String name
    ) {
    }

    public record ContractFile(
            @NotBlank String path,
            @NotEmpty List<@Valid ContractResult> contracts
    ) {
    }

    public record ContractResult(
            @NotBlank String name,
            @NotNull VerificationStatus status,
            @Nullable Map<String, Object> context
    ) {
    }

}
