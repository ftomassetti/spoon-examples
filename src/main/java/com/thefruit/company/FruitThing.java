package com.thefruit.company;


public class FruitThing implements com.strumenta.json.JsonSerializable {
    private java.util.List<java.lang.String> fruits;

    public java.util.List<java.lang.String> getFruits() {
        return fruits;
    }

    public void setFruits(java.util.List<java.lang.String> fruits) {
        this.fruits = fruits;
    }

    private java.util.List<com.thefruit.company.Veggie> vegetables;

    public java.util.List<com.thefruit.company.Veggie> getVegetables() {
        return vegetables;
    }

    public void setVegetables(java.util.List<com.thefruit.company.Veggie> vegetables) {
        this.vegetables = vegetables;
    }

    public com.google.gson.JsonObject serialize() {
        com.google.gson.JsonObject res = new com.google.gson.JsonObject();
        res.add("fruits", com.strumenta.json.SerializationUtils.serialize(fruits));
        res.add("vegetables", com.strumenta.json.SerializationUtils.serialize(vegetables));
        return res;
    }

    public static com.thefruit.company.FruitThing unserialize(com.google.gson.JsonObject json) {
        com.thefruit.company.FruitThing res = new com.thefruit.company.FruitThing();
        res.setFruits((java.util.List) com.strumenta.json.SerializationUtils.unserialize(json.get("fruits"), com.google.gson.reflect.TypeToken.getParameterized(java.util.List.class, java.lang.String.class)));
        res.setVegetables((java.util.List) com.strumenta.json.SerializationUtils.unserialize(json.get("vegetables"), com.google.gson.reflect.TypeToken.getParameterized(java.util.List.class, com.thefruit.company.Veggie.class)));
        return res;
    }
}
