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

import io.github.concordile.broker.entity.ContractDependencyRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcContractGraphRepository implements ContractGraphRepository {

    private final JdbcClient jdbcClient;

    @Override
    public List<ContractDependencyRow> findActiveContractDependencies() {
        // language=PostgreSQL
        var sql = jdbcClient.sql("""
                select
                    c.id            as contract_id,
                    c.name          as contract_name,
                    c.path          as contract_path,
                    consumer.id     as consumer_id,
                    consumer.group_id as consumer_group_id,
                    consumer.name   as consumer_name,
                    provider.id     as provider_id,
                    provider.group_id as provider_group_id,
                    provider.name   as provider_name
                from contracts c
                join applications provider on provider.id = c.provider_id
                join applications consumer on consumer.id = c.consumer_id
                where c.deleted_at is null
                  and provider.deleted_at is null
                  and consumer.deleted_at is null
                  and c.consumer_id is not null
                order by consumer.name, provider.name, c.name
                """);
        return sql
                .query(ContractDependencyRow.class)
                .list();
    }

}
