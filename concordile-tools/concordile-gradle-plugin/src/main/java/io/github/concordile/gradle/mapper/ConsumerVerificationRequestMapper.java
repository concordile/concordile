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

package io.github.concordile.gradle.mapper;

import io.github.concordile.broker.api.v1.CreateVerificationRequest;
import io.github.concordile.broker.api.v1.VerificationStatus;
import io.github.concordile.gradle.model.ConsumerVerificationContext;
import org.gradle.api.GradleException;

import java.util.Map;

public final class ConsumerVerificationRequestMapper {

    public CreateVerificationRequest map(
            ConsumerVerificationContext context,
            Map<String, VerificationStatus> testResults
    ) {
        return new CreateVerificationRequest(
                new CreateVerificationRequest.Party(
                        context.role(),
                        toApiApplication(context.application()),
                        context.version()
                ),
                context.counterparties().stream()
                        .map(counterparty -> new CreateVerificationRequest.Counterparty(
                                toApiApplication(counterparty.application()),
                                counterparty.version(),
                                counterparty.files().stream()
                                        .map(file -> new CreateVerificationRequest.ContractFile(
                                                file.path(),
                                                file.contracts().stream()
                                                        .map(contract -> new CreateVerificationRequest.ContractResult(
                                                                contract.name(),
                                                                resolveStatus(contract, testResults)
                                                        ))
                                                        .toList()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    private CreateVerificationRequest.Application toApiApplication(
            ConsumerVerificationContext.Application application
    ) {
        return new CreateVerificationRequest.Application(
                application.groupId(),
                application.name()
        );
    }

    private VerificationStatus resolveStatus(
            ConsumerVerificationContext.Contract contract,
            Map<String, VerificationStatus> testResults
    ) {
        var exactKey = contract.testClassName() + "#" + contract.testMethodName();
        var exactStatus = testResults.get(exactKey);

        if (exactStatus != null) {
            return exactStatus;
        }

        var simpleKey = simpleClassName(contract.testClassName()) + "#" + contract.testMethodName();
        var simpleStatus = testResults.get(simpleKey);

        if (simpleStatus != null) {
            return simpleStatus;
        }

        throw new GradleException("Cannot find test result for consumer contract '%s' using test method '%s#%s'"
                .formatted(contract.name(), contract.testClassName(), contract.testMethodName()));
    }

    private String simpleClassName(String className) {
        var lastDotIndex = className.lastIndexOf('.');
        return lastDotIndex == -1 ? className : className.substring(lastDotIndex + 1);
    }

}
