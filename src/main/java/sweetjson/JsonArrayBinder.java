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

public class JsonArrayBinder<T> implements JsonBinder<T> {
    public static final JsonBinder<?> INSTANCE = new JsonArrayBinder<>();

    private JsonArrayBinder () {
    }

    @SuppressWarnings("unchecked")
    public T construct (final JsonElement element, final Typedef<T> definition, final Bag bag) {
        final var list = element.arraylist();
        final var size = list.size();
        final var component_type = definition.klass().componentType();
        final var model = Array.newInstance(component_type, size);
        for (int i = 0; i < size; i++)
            Array.set(model, i, list.get(i).bind_to(component_type, bag));
        return (T)model;
    }
}
