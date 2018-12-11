import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.declaration.CtAnnotation
import spoon.reflect.declaration.CtAnnotationType
import spoon.reflect.declaration.CtClass

fun printTitle(title: String) {
    val length = 4 + title.length
    println("=".repeat(length))
    println("| $title |")
    println("=".repeat(length))
}

fun printList(elements: Sequence<*>) {
    elements.forEach {
        println(" * $it")
    }
}

fun examineClassesWithManyMethods(ctModel: CtModel, threshold: Int = 20) {
    val classes = ctModel.filterChildren<CtClass<*>> {
        it is CtClass<*> && it.methods.size > threshold
    }.list<CtClass<*>>()
    printTitle("Classes with more than $threshold methods")
    printList(classes.asSequence()
            .sortedByDescending { it.methods.size }
            .map { "${it.qualifiedName} (${it.methods.size})"})
    println()
}

fun CtClass<*>.isTestClass() = this.methods.any { it.annotations.any { it.annotationType.qualifiedName == "org.junit.Test" } }

fun verifyTestClassesHaveProperName(ctModel: CtModel) {
    val testClasses = ctModel.filterChildren<CtClass<*>> { it is CtClass<*> && it.isTestClass() }.list<CtClass<*>>()
    val testClassesNamedCorrectly = testClasses.filter { it.simpleName.endsWith("Test") }
    val testClassesNotNamedCorrectly = testClasses.filter { it !in testClassesNamedCorrectly }
    printTitle("Test classes named correctly")
    println("N Classes named correctly: ${testClassesNamedCorrectly.size}")
    println("N Classes not named correctly: ${testClassesNotNamedCorrectly.size}")
    printList(testClassesNotNamedCorrectly.asSequence().sortedBy { it.qualifiedName }.map { it.qualifiedName })
}

fun main(args: Array<String>) {
    val launcher = Launcher()
    launcher.addInputResource("codebases/jp/javaparser-core/src/main/java")
    launcher.addInputResource("codebases/jp/javaparser-core-testing/src/test/java")
    launcher.addInputResource("libs/junit-vintage-engine-4.12.3.jar")
    launcher.environment.noClasspath = true
    val model = launcher.buildModel()

    verifyTestClassesHaveProperName(model)
}
