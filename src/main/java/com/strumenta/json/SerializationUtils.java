package com.strumenta.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

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

    public static Object unserialize(JsonElement json, TypeToken<?> expectedType) {
        if (expectedType.getRawType().getCanonicalName().equals(List.class.getCanonicalName())) {
            List<Object> res = new LinkedList<>();
            for (JsonElement jsonElement : json.getAsJsonArray()) {
                ParameterizedType parameterizedType = (ParameterizedType)expectedType.getType();
                Type paramType = parameterizedType.getActualTypeArguments()[0];
                res.add(unserialize(jsonElement, TypeToken.get(paramType)));
            }
            return res;
        }
        if (expectedType.getRawType().getCanonicalName().equals(String.class.getCanonicalName())) {
            return json.getAsString();
        }
        if (expectedType.getRawType().getCanonicalName().equals(Boolean.class.getCanonicalName())
                || expectedType.getRawType().getCanonicalName().equals(boolean.class.getCanonicalName())) {
            return json.getAsBoolean();
        }
        if (JsonSerializable.class.isAssignableFrom(expectedType.getRawType())) {
            try {
                Method unserializeMethod = expectedType.getRawType().getMethod("unserialize", JsonObject.class);
                return unserializeMethod.invoke(null, json.getAsJsonObject());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + expectedType.getRawType().getCanonicalName() + " should have the method unserialize", e);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failure invoking method unserialize on " + expectedType.getRawType().getCanonicalName(), e);
            }
        }
        throw new UnsupportedOperationException("Expected type: " + expectedType);
    }
}
