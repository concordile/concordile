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

import io.github.concordile.broker.domain.DeploymentCheck;
import io.github.concordile.broker.domain.DeploymentCheckEvaluationStatus;
import io.github.concordile.broker.domain.DeploymentCheckStatus;
import io.github.concordile.broker.domain.VerificationStatus;
import io.github.concordile.broker.entity.DeploymentCheckEntity;
import io.github.concordile.broker.entity.DeploymentCheckEvaluationEntity;
import io.github.concordile.broker.entity.DeploymentCheckEvaluationResultEntity;
import io.github.concordile.broker.entity.DeploymentCheckEvaluationResultId;
import io.github.concordile.broker.entity.DeploymentRecordEntity;
import io.github.concordile.broker.entity.VerificationResultEntity;
import io.github.concordile.broker.mapper.DeploymentCheckEntityMapper;
import io.github.concordile.broker.repository.DeploymentCheckEvaluationRepository;
import io.github.concordile.broker.repository.DeploymentCheckEvaluationResultRepository;
import io.github.concordile.broker.repository.DeploymentCheckRepository;
import io.github.concordile.broker.service.command.CreateDeploymentCheckCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
class DefaultDeploymentCheckService implements DeploymentCheckService {

    private final ApplicationService applicationService;
    private final ContractService contractService;
    private final VerificationResultService verificationResultService;
    private final DeploymentTargetService deploymentTargetService;
    private final DeploymentRecordService deploymentRecordService;

    private final DeploymentCheckRepository checkRepository;
    private final DeploymentCheckEvaluationRepository evaluationRepository;
    private final DeploymentCheckEvaluationResultRepository evaluationResultRepository;

    private final DeploymentCheckEntityMapper entityMapper;

    private static DeploymentCheckEvaluationStatus evaluateStatus(
            List<UUID> contractIds,
            List<VerificationResultEntity> results
    ) {
        var resultByContractId = results.stream()
                .collect(Collectors.toMap(
                        VerificationResultEntity::getContractId,
                        Function.identity(),
                        (left, right) -> left
                ));

        var anyFailed = contractIds.stream()
                .map(resultByContractId::get)
                .filter(Objects::nonNull)
                .anyMatch(result -> VerificationStatus.FAILED.name().equals(result.getStatus()));

        if (anyFailed) {
            return DeploymentCheckEvaluationStatus.FAILED;
        }

        var anyMissing = contractIds.stream()
                .anyMatch(contractId -> !resultByContractId.containsKey(contractId));

        if (anyMissing) {
            return DeploymentCheckEvaluationStatus.MISSING;
        }

        return DeploymentCheckEvaluationStatus.PASSED;
    }

    private static DeploymentCheckStatus aggregateCheckStatus(List<PeerOutcome> peerOutcomes) {
        if (peerOutcomes.stream().anyMatch(o -> o.evaluationStatus() == DeploymentCheckEvaluationStatus.FAILED)) {
            return DeploymentCheckStatus.BLOCKED;
        }
        if (peerOutcomes.stream().anyMatch(o -> o.evaluationStatus() == DeploymentCheckEvaluationStatus.MISSING)) {
            return DeploymentCheckStatus.UNKNOWN;
        }
        return DeploymentCheckStatus.READY;
    }

    private static String reasonFor(DeploymentCheckEvaluationStatus status) {
        return switch (status) {
            case PASSED -> "All required verification results passed";
            case FAILED -> "At least one verification result failed";
            case MISSING -> "At least one required verification result is missing";
            case IGNORED -> "Evaluation ignored";
        };
    }

    @Override
    public DeploymentCheck create(CreateDeploymentCheckCommand command) {
        var target = deploymentTargetService.getByName(command.target());
        var application = command.application();
        var app = applicationService.findOrCreate(application.groupId(), application.name());

        var outcomes = evaluatePeers(target.id(), app.getId(), command.version());
        var status = aggregateCheckStatus(outcomes);

        var savedCheck = saveCheck(target.id(), app.getId(), command.version(), status);
        saveEvaluations(savedCheck.getId(), app.getId(), command.version(), outcomes);

        return entityMapper.mapEntity2Domain(savedCheck);
    }

    private List<PeerOutcome> evaluatePeers(UUID targetId, UUID appId, String appVersion) {
        return deploymentRecordService.findAllActiveByTargetId(targetId).stream()
                .filter(record -> !record.getAppId().equals(appId))
                .map(record -> evaluatePeer(appId, appVersion, record))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<PeerOutcome> evaluatePeer(UUID appId, String appVersion, DeploymentRecordEntity peer) {
        var contractIds = contractService.findIdsBetweenApps(appId, peer.getAppId());
        if (contractIds.isEmpty()) {
            return Optional.empty();
        }

        var results = verificationResultService.findLatestPerContractForPartyVersions(
                appId,
                appVersion,
                peer.getAppId(),
                peer.getAppVersion()
        );

        var status = evaluateStatus(contractIds, results);

        return Optional.of(new PeerOutcome(peer, status, results));
    }

    private DeploymentCheckEntity saveCheck(
            UUID targetId,
            UUID appId,
            String appVersion,
            DeploymentCheckStatus status
    ) {
        return checkRepository.save(DeploymentCheckEntity.builder()
                .targetId(targetId)
                .appId(appId)
                .appVersion(appVersion)
                .status(status.name())
                .context(Map.of())
                .build());
    }

    private void saveEvaluations(
            UUID checkId,
            UUID appId,
            String appVersion,
            List<PeerOutcome> outcomes
    ) {
        for (var outcome : outcomes) {
            var status = outcome.evaluationStatus();

            var savedEval = evaluationRepository.save(DeploymentCheckEvaluationEntity.builder()
                    .checkId(checkId)
                    .partyId(appId)
                    .partyVersion(appVersion)
                    .counterpartyId(outcome.deployment().getAppId())
                    .counterpartyVersion(outcome.deployment().getAppVersion())
                    .status(status.name())
                    .reason(reasonFor(status))
                    .context(Map.of())
                    .build());

            saveEvaluationResults(savedEval.getId(), outcome.verificationResults());
        }
    }

    private void saveEvaluationResults(UUID evaluationId, List<VerificationResultEntity> results) {
        var links = results.stream()
                .map(result -> DeploymentCheckEvaluationResultEntity.builder()
                        .id(new DeploymentCheckEvaluationResultId(evaluationId, result.getId()))
                        .build())
                .toList();

        if (!links.isEmpty()) {
            evaluationResultRepository.saveAll(links);
        }
    }

    private record PeerOutcome(
            DeploymentRecordEntity deployment,
            DeploymentCheckEvaluationStatus evaluationStatus,
            List<VerificationResultEntity> verificationResults
    ) {
    }

}
