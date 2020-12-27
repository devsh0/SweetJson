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

import java.util.HashMap;
import java.util.Map;

public class SweetJson
{
    private static final Map<Typedef<?>, JsonBinder<?>> CUSTOM_BINDERS = new HashMap<>();

    public static <T> void register_binder (final Typedef<T> definition, final JsonBinder<T> binder)
    {
        CUSTOM_BINDERS.put(definition, binder);
    }

    @SuppressWarnings("unchecked")
    public static <T> JsonBinder<T> get_binder (final Typedef<T> type)
    {
        var binder = (JsonBinder<T>) CUSTOM_BINDERS.get(type);
        if (binder != null) return binder;
        return type.is_json_primitive() ? (JsonBinder<T>) JsonPrimitiveBinder.INSTANCE
                : (type.is_array() ? (JsonBinder<T>) JsonArrayBinder.INSTANCE
                : (JsonBinder<T>) JsonObjectBinder.INSTANCE);
    }
}
