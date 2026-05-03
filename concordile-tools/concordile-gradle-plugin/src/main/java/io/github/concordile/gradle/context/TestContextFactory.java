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

package io.github.concordile.gradle.context;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TestContextFactory {

    public Map<String, Object> create(
            String testClassName,
            String testMethodName
    ) {
        var context = new LinkedHashMap<String, Object>();

        var test = new LinkedHashMap<String, Object>();
        test.put("className", testClassName);
        test.put("methodName", testMethodName);

        context.put("test", test);

        return context;
    }

}
