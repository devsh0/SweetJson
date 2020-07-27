/*
 * Copyright (C) 2020 Devashish Jaiswal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sweetjson;

import java.util.Map;

public class JsonObjectBinder<T> implements JsonBinder<T> {
    public static final JsonBinder<?> INSTANCE = new JsonObjectBinder<>();

    public T construct (final JsonElement element, final Typedef<T> definition, final Bag bag) {
        try {
            Map<String, JsonElement> m_map = element.map();
            var model = definition.create_instance();
            var serializable_fields = JsonSerializationUtils.get_serializable_fields(definition.klass());

            for (var entry : m_map.entrySet()) {
                var field = serializable_fields.get(entry.getKey());
                if (field != null) {
                    field.setAccessible(true);
                    var field_type = JsonSerializationUtils.get_field_typedef(field, definition);
                    var binder = SweetJson.get_binder(field_type);
                    field.set(model, binder.construct(entry.getValue(), field_type, bag));
                }
            }

            return model;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
