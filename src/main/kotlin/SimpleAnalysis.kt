import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod

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

fun main(args: Array<String>) {
    val launcher = Launcher()
    launcher.addInputResource("codebases/jp/javaparser-core/src/main/java")
    launcher.environment.noClasspath = true
    val model = launcher.buildModel()

    examineClassesWithManyMethods(model)
}
