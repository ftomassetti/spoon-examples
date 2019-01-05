package com.thefruit.company;


import com.google.gson.reflect.TypeToken;

public class Veggie implements com.strumenta.json.JsonSerializable {
    private java.lang.String veggieName;

    public java.lang.String getVeggieName() {
        return veggieName;
    }

    public void setVeggieName(java.lang.String veggieName) {
        this.veggieName = veggieName;
    }

    private boolean veggieLike;

    public boolean getVeggieLike() {
        return veggieLike;
    }

    public void setVeggieLike(boolean veggieLike) {
        this.veggieLike = veggieLike;
    }

    public com.google.gson.JsonObject serialize() {
        com.google.gson.JsonObject res = new com.google.gson.JsonObject();
        res.add("veggieName", com.strumenta.json.SerializationUtils.serialize(veggieName));
        res.add("veggieLike", com.strumenta.json.SerializationUtils.serialize(veggieLike));
        return res;
    }

    public static com.thefruit.company.Veggie unserialize(com.google.gson.JsonObject json) {
        com.thefruit.company.Veggie res = new com.thefruit.company.Veggie();
        res.setVeggieName((String)com.strumenta.json.SerializationUtils.unserialize(json.get("veggieName"), TypeToken.get(String.class)));
        res.setVeggieLike((Boolean)com.strumenta.json.SerializationUtils.unserialize(json.get("veggieLike"), TypeToken.get(Boolean.class)));
        return res;
    }
}