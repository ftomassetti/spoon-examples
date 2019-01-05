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
}
