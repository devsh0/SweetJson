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

import java.lang.reflect.Array;

public class JsonArrayBinder<T> implements JsonBinder<T>
{
    public static final JsonBinder<?> INSTANCE = new JsonArrayBinder<>();

    @SuppressWarnings("unchecked")
    public T construct (final JsonValue element, final Typedef<T> definition, final Bag bag)
    {
        final var list = element.as_list();
        final var component_type = definition.klass().componentType();
        int size_without_null = 0;
        for (var entry : list) size_without_null += entry.is_null() ? 0 : 1;
        final var model = Array.newInstance(component_type, size_without_null);
        int i = 0, j = 0;
        while (j < list.size())
        {
            var entry = list.get(j);
            if (!entry.is_null())
                Array.set(model, i++, entry.bind_to(component_type, bag));
            j++;
        }
        return (T) model;
    }
}
