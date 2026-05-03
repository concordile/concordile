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

package io.github.concordile.gradle;

public final class ConcordilePluginConstants {

    @SuppressWarnings("unused")
    public static final String PLUGIN_ID = "io.github.concordile";
    public static final String PLUGIN_VERSION = "0.1.0-SNAPSHOT";
    public static final String EXTENSION_NAME = "concordile";
    public static final String GROUP_NAME = "concordile";

    public static final String SPRING_CLOUD_CONTRACT_PLUGIN_ID = "org.springframework.cloud.contract";
    public static final String SPRING_CLOUD_CONTRACT_CONSUMER_EXTENSION_GAV = "io.github.concordile.spring:concordile-spring-cloud-contract-consumer-extension:" + PLUGIN_VERSION;
    public static final String SPRING_CLOUD_CONTRACT_PRODUCER_EXTENSION_GAV = "io.github.concordile.spring:concordile-spring-cloud-contract-provider-extension:" + PLUGIN_VERSION;

    public static final String PREPARE_CONSUMER_VERIFICATION_CONTEXT_TASK_NAME = "prepareConcordileConsumerVerificationContext";
    public static final String PREPARE_PRODUCER_VERIFICATION_CONTEXT_TASK_NAME = "prepareConcordileProducerVerificationContext";
    public static final String PUBLISH_CONSUMER_VERIFICATION_TASK_NAME = "publishConcordileConsumerVerification";
    public static final String PUBLISH_PRODUCER_VERIFICATION_TASK_NAME = "publishConcordileProducerVerification";

    public static final String TEST_TASK_NAME = "test";
    public static final String GENERATE_CONTRACT_TESTS_TASK_NAME = "generateContractTests";
    public static final String CONTRACT_TEST_TASK_NAME = "contractTest";

    public static final String CONSUMER_VERIFICATION_CONTEXT_FILE = "concordile/consumer-verification-context.json";
    public static final String PRODUCER_VERIFICATION_CONTEXT_FILE = "concordile/producer-verification-context.json";

    private ConcordilePluginConstants() {
    }

}
