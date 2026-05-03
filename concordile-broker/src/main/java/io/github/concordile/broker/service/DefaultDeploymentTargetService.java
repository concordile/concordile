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

import io.github.concordile.broker.domain.DeploymentTarget;
import io.github.concordile.broker.entity.DeploymentTargetEntity;
import io.github.concordile.broker.exception.EntityNotFoundException;
import io.github.concordile.broker.mapper.DeploymentTargetEntityMapper;
import io.github.concordile.broker.repository.DeploymentTargetRepository;
import io.github.concordile.broker.service.command.CreateDeploymentTargetCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
class DefaultDeploymentTargetService implements DeploymentTargetService {

    private final DeploymentTargetRepository repository;
    private final DeploymentTargetEntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DeploymentTarget> findAll(Pageable pageable) {
        return repository.findAllByDeletedAtIsNull(pageable)
                .map(entityMapper::mapEntity2Domain);
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentTarget getByName(String name) {
        return repository.findActiveByName(name)
                .map(entityMapper::mapEntity2Domain)
                .orElseThrow(() -> new EntityNotFoundException("Deployment target not found: " + name));
    }

    @Override
    public DeploymentTarget create(CreateDeploymentTargetCommand command) {
        var saved = repository.save(DeploymentTargetEntity.builder()
                .name(command.name())
                .context(command.context() == null ? Map.of() : command.context())
                .build());
        return entityMapper.mapEntity2Domain(saved);
    }

}
