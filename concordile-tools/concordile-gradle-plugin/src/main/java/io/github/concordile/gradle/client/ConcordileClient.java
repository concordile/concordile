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

package io.github.concordile.gradle.client;

import io.github.concordile.broker.api.v1.CreateVerificationRequest;
import org.gradle.api.GradleException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ConcordileClient {

    private final JsonMapper jsonMapper = new JsonMapper();

    public void createVerification(
            String brokerUrl,
            CreateVerificationRequest request
    ) throws IOException, InterruptedException {
        var body = jsonMapper.writeValueAsString(request);
        var endpoint = normalizeBrokerUrl(brokerUrl) + "/api/v1/verifications";

        var httpRequest = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = HttpClient.newHttpClient().send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GradleException("""
                    Failed to publish Concordile producer verification.
                    
                    Status: %s
                    Body: %s
                    """.formatted(response.statusCode(), response.body()));
        }
    }

    private String normalizeBrokerUrl(String brokerUrl) {
        if (brokerUrl.endsWith("/")) {
            return brokerUrl.substring(0, brokerUrl.length() - 1);
        }

        return brokerUrl;
    }

}
