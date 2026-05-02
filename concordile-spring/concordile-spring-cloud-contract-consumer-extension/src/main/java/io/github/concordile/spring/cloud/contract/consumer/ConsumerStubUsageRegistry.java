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

package io.github.concordile.spring.cloud.contract.consumer;

import io.github.concordile.spring.cloud.contract.api.ConsumerStubUsageContext;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class ConsumerStubUsageRegistry {

    private static final AtomicReference<@Nullable CurrentTestUsage> CURRENT = new AtomicReference<>();

    private ConsumerStubUsageRegistry() {
    }

    static void begin(
            String testClassName,
            String testMethodName
    ) {
        CURRENT.set(new CurrentTestUsage(
                testClassName,
                testMethodName
        ));
    }

    static void recordMatchedMappingId(String mappingId) {
        var current = CURRENT.get();

        if (current == null) {
            return;
        }

        current.record(mappingId);
    }

    static @Nullable ConsumerStubUsageContext snapshot() {
        var current = CURRENT.get();

        if (current == null) {
            return null;
        }

        return current.toContext();
    }

    static void end() {
        CURRENT.set(null);
    }

    private static final class CurrentTestUsage {

        private final String testClassName;

        private final String testMethodName;

        private final LinkedHashSet<String> matchedMappingIds = new LinkedHashSet<>();

        private CurrentTestUsage(
                String testClassName,
                String testMethodName
        ) {
            this.testClassName = testClassName;
            this.testMethodName = testMethodName;
        }

        private synchronized void record(@Nullable String mappingId) {
            if (mappingId == null || mappingId.isBlank()) {
                return;
            }

            matchedMappingIds.add(mappingId);
        }

        private synchronized ConsumerStubUsageContext toContext() {
            return new ConsumerStubUsageContext(
                    testClassName,
                    testMethodName,
                    List.copyOf(matchedMappingIds)
            );
        }

    }

}
