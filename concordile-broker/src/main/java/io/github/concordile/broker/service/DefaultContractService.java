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

import io.github.concordile.broker.entity.ContractEntity;
import io.github.concordile.broker.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultContractService implements ContractService {

    private final ContractRepository contractRepository;

    @Override
    public ContractEntity findOrCreate(
            UUID producerId,
            UUID consumerId,
            String path,
            String name
    ) {
        return contractRepository.findByProducerIdAndConsumerIdAndPathAndName(
                        producerId,
                        consumerId,
                        path,
                        name
                )
                .orElseGet(() -> create(
                        producerId,
                        consumerId,
                        path,
                        name
                ));
    }

    @Override
    public ContractEntity create(
            UUID producerId,
            UUID consumerId,
            String path,
            String name
    ) {
        var entity = ContractEntity.builder()
                .producerId(producerId)
                .consumerId(consumerId)
                .path(path)
                .name(name)
                .build();
        return contractRepository.save(entity);
    }

}
