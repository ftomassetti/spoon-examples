package com.thefruit.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Arrays;

public class Example {

    public static void main(String[] args) {
        FruitThing ft = new FruitThing();
        ft.setFruits(Arrays.asList("Banana", "Pear", "Apple"));
        Veggie cucumber = new Veggie();
        cucumber.setVeggieLike(false);
        cucumber.setVeggieName("Cucumber");
        Veggie carrot = new Veggie();
        carrot.setVeggieLike(true);
        carrot.setVeggieName("Carrot");
        ft.setVegetables(Arrays.asList(cucumber, carrot));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(ft.serialize()));

        JsonElement serialized = ft.serialize();
        FruitThing unserializedFt = FruitThing.unserialize(serialized.getAsJsonObject());
        System.out.println("Fruits: " + unserializedFt.getFruits());
    }
}
