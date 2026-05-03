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

import io.github.concordile.broker.entity.DeploymentRecordEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeploymentRecordRepository
        extends ListCrudRepository<DeploymentRecordEntity, UUID> {

    // language=PostgreSQL
    @Query("""
            select *
            from deployment_records
            where target_id = :targetId
              and app_id = :appId
              and status = 'ACTIVE'
              and deleted_at is null
            limit 1
            """)
    Optional<DeploymentRecordEntity> findActiveByTargetIdAndAppId(
            UUID targetId,
            UUID appId
    );

}
