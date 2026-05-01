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

import io.github.concordile.broker.entity.ApplicationEntity;
import io.github.concordile.broker.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultApplicationService implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    public ApplicationEntity findOrCreate(String groupId, String name) {
        return applicationRepository.findByGroupIdAndName(groupId, name)
                .orElseGet(() -> create(groupId, name));
    }

    @Override
    public ApplicationEntity create(String groupId, String name) {
        var entity = ApplicationEntity.builder()
                .groupId(groupId)
                .name(name)
                .build();
        return applicationRepository.save(entity);
    }

}
