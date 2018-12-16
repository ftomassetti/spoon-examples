package com.thefruit.company;

//public class Veggie {
//    private java.lang.String veggieName;
//
//    private boolean veggieLike;
//
//    void unserialize() {}
//
//    public com.google.gson.JsonObject serialize() {
//        com.google.gson.JsonObject res = new com.google.gson.JsonObject();
//        res.addProperty("veggieName", veggieName);
//        res.addProperty("veggieLike", veggieLike);
//        return res;
//    }
//}
//public class FruitThing {
//    private java.util.List<java.lang.String> fruits;
//
//    private java.util.List<com.thefruit.company.Veggie> vegetables;
//
//    void unserialize() {}
//
//    public com.google.gson.JsonObject serialize() {
//        com.google.gson.JsonObject res = new com.google.gson.JsonObject();
//        {
//            com.google.gson.JsonArray jsonArray = new com.google.gson.JsonArray();
//            res.addProperty("fruits", jsonArray);
//        }
//        {
//            com.google.gson.JsonArray jsonArray = new com.google.gson.JsonArray();
//            res.addProperty("vegetables", jsonArray);
//        }
//        return res;
//    }
//}