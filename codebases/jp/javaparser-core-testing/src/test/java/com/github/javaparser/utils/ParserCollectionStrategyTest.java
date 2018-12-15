package com.github.javaparser.utils;

import org.junit.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;


public class ParserCollectionStrategyTest {

    private final Path root = CodeGenerationUtils.mavenModuleRoot(ParserCollectionStrategyTest.class).resolve("").getParent();
    private final ProjectRoot projectRoot = new ParserCollectionStrategy().collect(root);

    @Test
    public void getSourceRoots() {
        assertFalse(projectRoot.getSourceRoots().size() == 0);
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve("javaparser-core/src/com.strumenta.spoonexamples.main/java")));
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve
                ("javaparser-core-generators/src/com.strumenta.spoonexamples.main/java")));
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve
                ("javaparser-core-metamodel-generator/src/com.strumenta.spoonexamples.main/java")));
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve
                ("javaparser-symbol-solver-core/src/com.strumenta.spoonexamples.main/java")));
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve
                ("javaparser-symbol-solver-logic/src/com.strumenta.spoonexamples.main/java")));
        assertNotEquals(Optional.empty(), projectRoot.getSourceRoot(root.resolve
                ("javaparser-symbol-solver-model/src/com.strumenta.spoonexamples.main/java")));
    }
}
