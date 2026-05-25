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

import io.github.concordile.broker.api.v1.ContractGraphResponse;
import io.github.concordile.broker.repository.ContractGraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultContractGraphService implements ContractGraphService {

    private final ContractGraphRepository repository;

    @Override
    public ContractGraphResponse getContractTopologyGraph() {
        var rows = repository.findActiveContractDependencies();
        var nodesByApplicationId = new HashMap<UUID, NodeDraft>();
        var edgesByConsumerAndProvider = new HashMap<String, EdgeDraft>();

        for (var row : rows) {
            nodesByApplicationId.putIfAbsent(
                    row.consumerId(),
                    new NodeDraft(row.consumerId(), row.consumerGroupId(), row.consumerName())
            );
            nodesByApplicationId.putIfAbsent(
                    row.providerId(),
                    new NodeDraft(row.providerId(), row.providerGroupId(), row.providerName())
            );

            var edgeKey = row.consumerId() + ":" + row.providerId();
            var edge = edgesByConsumerAndProvider.computeIfAbsent(
                    edgeKey,
                    k -> new EdgeDraft(row.consumerId(), row.providerId())
            );
            edge.contracts.add(new ContractGraphResponse.Edge.Contract(
                    row.contractId(),
                    row.contractName(),
                    row.contractPath()
            ));
        }

        var nodes = nodesByApplicationId.values().stream()
                .map(d -> new ContractGraphResponse.Node(
                        d.applicationId(),
                        d.applicationId(),
                        d.groupId(),
                        d.name()
                ))
                .sorted(Comparator
                        .comparing(ContractGraphResponse.Node::name)
                        .thenComparing(ContractGraphResponse.Node::groupId)
                        .thenComparing(ContractGraphResponse.Node::id))
                .toList();

        var nameByApplicationId = new HashMap<UUID, String>();
        for (var draft : nodesByApplicationId.values()) {
            nameByApplicationId.put(draft.applicationId(), draft.name());
        }

        var edges = edgesByConsumerAndProvider.values().stream()
                .map(edgeDraft -> {
                    var sortedContracts = edgeDraft.contracts.stream()
                            .sorted(Comparator
                                    .comparing(ContractGraphResponse.Edge.Contract::name)
                                    .thenComparing(ContractGraphResponse.Edge.Contract::path)
                                    .thenComparing(ContractGraphResponse.Edge.Contract::id))
                            .toList();
                    return new ContractGraphResponse.Edge(
                            edgeDraft.consumerId + ":" + edgeDraft.providerId,
                            edgeDraft.consumerId,
                            edgeDraft.providerId,
                            sortedContracts
                    );
                })
                .sorted(Comparator
                        .comparing((ContractGraphResponse.Edge e) ->
                                nameByApplicationId.get(e.consumerId()))
                        .thenComparing(e -> nameByApplicationId.get(e.providerId())))
                .toList();

        return new ContractGraphResponse("CONTRACT", nodes, edges);
    }

    private record NodeDraft(UUID applicationId, String groupId, String name) {
    }

    private static final class EdgeDraft {

        final UUID consumerId;
        final UUID providerId;
        final List<ContractGraphResponse.Edge.Contract> contracts = new ArrayList<>();

        EdgeDraft(UUID consumerId, UUID providerId) {
            this.consumerId = consumerId;
            this.providerId = providerId;
        }

    }

}
