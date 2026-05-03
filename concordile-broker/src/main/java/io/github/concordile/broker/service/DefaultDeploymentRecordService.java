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

import io.github.concordile.broker.domain.DeploymentRecord;
import io.github.concordile.broker.domain.DeploymentRecordStatus;
import io.github.concordile.broker.entity.DeploymentRecordEntity;
import io.github.concordile.broker.mapper.DeploymentRecordEntityMapper;
import io.github.concordile.broker.repository.DeploymentRecordRepository;
import io.github.concordile.broker.service.command.CreateDeploymentRecordCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
class DefaultDeploymentRecordService implements DeploymentRecordService {

    private final DeploymentTargetService deploymentTargetService;
    private final ApplicationService applicationService;

    private final DeploymentRecordRepository repository;
    private final DeploymentRecordEntityMapper entityMapper;

    @Override
    public DeploymentRecord create(CreateDeploymentRecordCommand command) {
        var target = deploymentTargetService.getByName(command.target());

        var application = command.application();
        var appEntity = applicationService.findOrCreate(application.groupId(), application.name());

        repository.findActiveByTargetIdAndAppId(target.id(), appEntity.getId()).ifPresent(active -> {
            active.setStatus(DeploymentRecordStatus.REPLACED.name());
            repository.save(active);
        });

        var saved = repository.save(DeploymentRecordEntity.builder()
                .targetId(target.id())
                .appId(appEntity.getId())
                .appVersion(command.version())
                .status(DeploymentRecordStatus.ACTIVE.name())
                .context(Map.of())
                .build());

        return entityMapper.mapEntity2Domain(saved);
    }

    @Override
    public List<DeploymentRecordEntity> findAllActiveByTargetId(UUID targetId) {
        return repository.findAllActiveByTargetId(targetId);
    }

}
