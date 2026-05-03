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

package io.github.concordile.broker.repository;

import io.github.concordile.broker.entity.ContractEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository
        extends ListCrudRepository<ContractEntity, UUID> {

    Optional<ContractEntity> findByProviderIdAndConsumerIdAndPathAndName(
            UUID providerId,
            UUID consumerId,
            String path,
            String name
    );

    // language=PostgreSQL
    @Query("""
            select c.id
            from contracts c
            where c.deleted_at is null
              and c.consumer_id is not null
              and (
                (c.provider_id = :appIdA and c.consumer_id = :appIdB)
                or (c.provider_id = :appIdB and c.consumer_id = :appIdA)
              )
            """)
    List<UUID> findContractIdsBetweenApps(UUID appIdA, UUID appIdB);

}
