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

package io.github.concordile.broker.service;

import io.github.concordile.broker.domain.Verification;
import io.github.concordile.broker.domain.VerificationPartyRole;
import io.github.concordile.broker.domain.VerificationResult;
import io.github.concordile.broker.domain.VerificationStatus;
import io.github.concordile.broker.entity.ApplicationEntity;
import io.github.concordile.broker.entity.VerificationEntity;
import io.github.concordile.broker.entity.VerificationResultEntity;
import io.github.concordile.broker.mapper.VerificationEntityMapper;
import io.github.concordile.broker.mapper.VerificationResultEntityMapper;
import io.github.concordile.broker.repository.VerificationRepository;
import io.github.concordile.broker.repository.VerificationResultRepository;
import io.github.concordile.broker.service.command.CreateVerificationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultVerificationService implements VerificationService {

    private final ApplicationService applicationService;
    private final ContractService contractService;

    private final VerificationRepository verificationRepository;
    private final VerificationResultRepository verificationResultRepository;

    private final VerificationEntityMapper verificationEntityMapper;
    private final VerificationResultEntityMapper verificationResultEntityMapper;

    @Override
    public Verification create(CreateVerificationCommand command) {
        var application = command.party();
        var partyEntity = applicationService.findOrCreate(application.groupId(), application.name());

        var verificationEntity = verificationRepository.save(VerificationEntity.builder()
                .partyRole(command.partyRole().name())
                .partyId(partyEntity.getId())
                .partyVersion(command.partyVersion())
                .status(resolveVerificationStatus(command).name())
                .build());

        var results = createResults(command, partyEntity, verificationEntity.getId());

        return verificationEntityMapper.mapEntity2Domain(verificationEntity)
                .withResults(results);
    }

    private List<VerificationResult> createResults(
            CreateVerificationCommand command,
            ApplicationEntity partyEntity,
            UUID verificationId
    ) {
        var results = new ArrayList<VerificationResult>();

        for (var counterparty : command.counterparties()) {
            results.addAll(createResultsForCounterparty(
                    command.partyRole(),
                    partyEntity,
                    counterparty,
                    verificationId
            ));
        }

        return results;
    }

    private List<VerificationResult> createResultsForCounterparty(
            VerificationPartyRole partyRole,
            ApplicationEntity partyEntity,
            CreateVerificationCommand.Counterparty counterparty,
            UUID verificationId
    ) {
        var application = counterparty.application();
        var counterpartyEntity = applicationService.findOrCreate(application.groupId(), application.name());
        var results = new ArrayList<VerificationResult>();

        for (var file : counterparty.files()) {
            results.addAll(createResultsForFile(
                    partyRole,
                    partyEntity,
                    counterpartyEntity,
                    counterparty.version(),
                    file,
                    verificationId
            ));
        }

        return results;
    }

    private List<VerificationResult> createResultsForFile(
            VerificationPartyRole partyRole,
            ApplicationEntity partyEntity,
            ApplicationEntity counterpartyEntity,
            String counterpartyVersion,
            CreateVerificationCommand.ContractFile file,
            UUID verificationId
    ) {
        var results = new ArrayList<VerificationResult>();

        for (var contractResult : file.contracts()) {
            var result = createResult(
                    partyRole,
                    partyEntity,
                    counterpartyEntity,
                    counterpartyVersion,
                    file.path(),
                    contractResult,
                    verificationId
            );

            results.add(result);
        }

        return results;
    }

    private VerificationResult createResult(
            VerificationPartyRole partyRole,
            ApplicationEntity partyEntity,
            ApplicationEntity counterpartyEntity,
            String counterpartyVersion,
            String contractPath,
            CreateVerificationCommand.ContractResult contractResult,
            UUID verificationId
    ) {
        var contractParties = resolveContractParties(partyRole, partyEntity, counterpartyEntity);

        var contractEntity = contractService.findOrCreate(
                contractParties.producerId(),
                contractParties.consumerId(),
                contractPath,
                contractResult.name()
        );

        var resultEntity = VerificationResultEntity.builder()
                .verificationId(verificationId)
                .contractId(contractEntity.getId())
                .counterpartyVersion(counterpartyVersion)
                .status(contractResult.status().name())
                .build();

        var savedResultEntity = verificationResultRepository.save(resultEntity);

        return verificationResultEntityMapper.mapEntity2Domain(savedResultEntity);
    }

    private ContractParties resolveContractParties(
            VerificationPartyRole partyRole,
            ApplicationEntity partyEntity,
            ApplicationEntity counterpartyEntity
    ) {
        return switch (partyRole) {
            case PRODUCER -> new ContractParties(
                    partyEntity.getId(),
                    counterpartyEntity.getId()
            );
            case CONSUMER -> new ContractParties(
                    counterpartyEntity.getId(),
                    partyEntity.getId()
            );
        };
    }

    private VerificationStatus resolveVerificationStatus(CreateVerificationCommand command) {
        var hasFailedResult = command.counterparties().stream()
                .flatMap(counterparty -> counterparty.files().stream())
                .flatMap(file -> file.contracts().stream())
                .anyMatch(contract -> contract.status() == VerificationStatus.FAILED);

        return hasFailedResult
                ? VerificationStatus.FAILED
                : VerificationStatus.PASSED;
    }

    private record ContractParties(
            UUID producerId,
            UUID consumerId
    ) {
    }

}
