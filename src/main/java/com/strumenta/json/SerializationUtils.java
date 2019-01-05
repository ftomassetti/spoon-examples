package com.strumenta.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class SerializationUtils {
    public static JsonElement serialize(Object value) {
        if (value instanceof JsonSerializable) {
            return ((JsonSerializable) value).serialize();
        }
        if (value instanceof Iterable<?>) {
            com.google.gson.JsonArray jsonArray = new com.google.gson.JsonArray();
            for (Object element : (Iterable<?>)value) {
                jsonArray.add(com.strumenta.json.SerializationUtils.serialize(element));
            }
            return jsonArray;
        }
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean)value);
        }
        if (value instanceof String) {
            return new JsonPrimitive((String)value);
        }
        throw new UnsupportedOperationException("Value: " + value + " (" + value.getClass().getCanonicalName() + ")");
    }
}
