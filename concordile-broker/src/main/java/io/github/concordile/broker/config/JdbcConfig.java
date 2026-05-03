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

package io.github.concordile.broker.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<?> userConverters() {
        return List.of(
                new MapToJsonbConverter(),
                new JsonbToMapConverter()
        );
    }

    @WritingConverter
    static class MapToJsonbConverter implements Converter<Map<String, Object>, PGobject> {

        private final JsonMapper jsonMapper = new JsonMapper();

        @Override
        public PGobject convert(Map<String, Object> source) {
            try {
                var jsonObject = new PGobject();
                jsonObject.setType("jsonb");
                jsonObject.setValue(jsonMapper.writeValueAsString(source));
                return jsonObject;
            } catch (SQLException exception) {
                throw new IllegalArgumentException("Failed to create jsonb object", exception);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to serialize jsonb value", exception);
            }
        }

    }

    @ReadingConverter
    static class JsonbToMapConverter implements Converter<PGobject, Map<String, Object>> {

        private final JsonMapper jsonMapper = new JsonMapper();
        private final TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };

        @Override
        public Map<String, Object> convert(PGobject source) {
            try {
                if (source.getValue() == null || source.getValue().isBlank()) {
                    return Map.of();
                }
                return jsonMapper.readValue(source.getValue(), typeReference);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to deserialize jsonb value", exception);
            }
        }

    }

}
