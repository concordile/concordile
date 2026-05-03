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
import io.github.concordile.broker.domain.DeploymentCheckStatus;
import io.github.concordile.broker.entity.DeploymentCheckEntity;
import io.github.concordile.broker.exception.EntityNotFoundException;
import io.github.concordile.broker.mapper.DeploymentCheckEntityMapper;
import io.github.concordile.broker.repository.DeploymentCheckRepository;
import io.github.concordile.broker.repository.DeploymentTargetRepository;
import io.github.concordile.broker.service.command.CreateDeploymentCheckCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
class DefaultDeploymentCheckService implements DeploymentCheckService {

    private final DeploymentTargetRepository deploymentTargetRepository;
    private final ApplicationService applicationService;
    private final DeploymentCheckRepository deploymentCheckRepository;
    private final DeploymentCheckEntityMapper deploymentCheckEntityMapper;

    @Override
    public DeploymentCheck create(CreateDeploymentCheckCommand command) {
        var targetEntity = deploymentTargetRepository.findByNameAndDeletedAtIsNull(command.target())
                .orElseThrow(() -> new EntityNotFoundException("Deployment target not found: " + command.target()));

        var application = command.application();
        var appEntity = applicationService.findOrCreate(application.groupId(), application.name());

        var saved = deploymentCheckRepository.save(DeploymentCheckEntity.builder()
                .targetId(targetEntity.getId())
                .appId(appEntity.getId())
                .appVersion(command.version())
                .status(DeploymentCheckStatus.READY.name()) // FIXME: evaluate
                .context(Map.of())
                .build());

        return deploymentCheckEntityMapper.mapEntity2Domain(saved);
    }

}
