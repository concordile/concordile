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

package io.github.concordile.broker.controller.v1;

import io.github.concordile.broker.api.v1.CreateDeploymentRecordRequest;
import io.github.concordile.broker.api.v1.DeploymentRecordResponse;
import io.github.concordile.broker.mapper.v1.CreateDeploymentRecordRequestMapper;
import io.github.concordile.broker.mapper.v1.DeploymentRecordResponseMapper;
import io.github.concordile.broker.service.DeploymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class DeploymentRecordController implements DeploymentRecordApi {

    private final DeploymentRecordService service;

    private final CreateDeploymentRecordRequestMapper createRequestMapper;
    private final DeploymentRecordResponseMapper responseMapper;

    @Override
    public ResponseEntity<DeploymentRecordResponse> createDeployment(
            CreateDeploymentRecordRequest request
    ) {
        var command = createRequestMapper.mapRequest2Command(request);
        var domain = service.create(command);
        var response = responseMapper.mapDomain2Response(domain);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

}
