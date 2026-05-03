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

import io.github.concordile.broker.entity.VerificationResultEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationResultRepository
        extends ListCrudRepository<VerificationResultEntity, UUID> {

    // language=PostgreSQL
    @Query("""
            select distinct on (vr.contract_id) vr.*
            from verification_results vr
            inner join verifications v on v.id = vr.verification_id
            inner join contracts c on c.id = vr.contract_id
            where v.deleted_at is null
              and vr.deleted_at is null
              and c.deleted_at is null
              and c.consumer_id is not null
              and (
                (c.provider_id = :appIdA and c.consumer_id = :appIdB)
                or (c.provider_id = :appIdB and c.consumer_id = :appIdA)
              )
              and (
                (v.party_id = :appIdA and v.party_version = :versionA
                  and vr.counterparty_version is not distinct from :versionB)
                or (v.party_id = :appIdB and v.party_version = :versionB
                  and vr.counterparty_version is not distinct from :versionA)
              )
            order by vr.contract_id, v.created_at desc, vr.created_at desc
            """)
    List<VerificationResultEntity> findLatestPerContractForPartyVersions(
            UUID appIdA,
            String versionA,
            UUID appIdB,
            String versionB
    );

}
