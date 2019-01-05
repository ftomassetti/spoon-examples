package com.strumenta.spoonexamples

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.everit.json.schema.*
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import spoon.reflect.code.CtBodyHolder
import spoon.reflect.code.CtForEach
import spoon.reflect.code.CtStatement
import spoon.reflect.cu.CompilationUnit
import spoon.reflect.declaration.*
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.support.StandardEnvironment
import spoon.support.reflect.code.CtForEachImpl
import spoon.support.reflect.cu.CompilationUnitImpl
import spoon.support.reflect.declaration.*
import spoon.support.reflect.reference.CtTypeReferenceImpl
import java.io.File
import java.io.InputStream
import java.util.*

class Dummy

fun CtClass<*>.addProperty(name: String, schema: Schema, classProvider: ClassProvider) {
    val field = CtFieldImpl<Any>().let {
        it.setSimpleName<CtField<Any>>(name)
        it.setType<CtField<Any>>(schema.toType(classProvider))
        it.setVisibility<CtField<Any>>(ModifierKind.PRIVATE)
    }
    this.addField<Any, Nothing>(field)

    // TODO generate getters
    // TODO generate setters
}

private fun Schema.toType(classProvider: ClassProvider): CtTypeReference<Any> {
    return when (this) {
        is ObjectSchema -> classProvider.typeReference(this)
        is ArraySchema -> createTypeReference(List::class.java).let {
            it.setActualTypeArguments<CtTypeReference<Any>>(listOf(this.allItemSchema.toType(classProvider)))
            it
        }
        is StringSchema -> createTypeReference(String::class.java)
        is BooleanSchema -> createTypeReference(Boolean::class.java)
        is ReferenceSchema -> referredSchema.toType(classProvider)
        else -> TODO("not implemented: ${this.javaClass}")
    }
}

private fun Schema.generateClassRecursively(classProvider: ClassProvider, name: String? = null) {
    when (this) {
        is ObjectSchema -> {
            classProvider.register(this, this.generateClass(classProvider, name))
            this.propertySchemas.forEach { it.value.generateClassRecursively(classProvider) }
        }
        is ArraySchema -> this.allItemSchema.generateClassRecursively(classProvider)
        is StringSchema, is BooleanSchema -> null
        is ReferenceSchema -> this.referredSchema.generateClassRecursively(classProvider)
        else -> TODO("not implemented: ${this.javaClass}")
    }
}

private fun ObjectSchema.generateClass(classProvider: ClassProvider, name: String? = null)
        : CtClass<Any> {
    return CtClassImpl<Any>().let { ctClass ->
        val packag = classProvider.pack
        packag.types.add(ctClass)
        ctClass.setParent(packag)
        ctClass.setVisibility<CtModifiable>(ModifierKind.PUBLIC)
        ctClass.setSimpleName<CtClass<Any>>(name ?: this.schemaLocation.split("/").last().capitalize())
        this.propertySchemas.forEach {
            ctClass.addProperty(it.key, it.value, classProvider)
        }
        addSerializeMethod(ctClass, this, classProvider)
        // addUnserializeMethod(ctClass, this)
        CtTypeReferenceImpl<Any>().let {
            it.setPackage<CtTypeReferenceImpl<Any>>(ctClass.`package`.reference)
            it.setSimpleName<CtTypeReferenceImpl<Any>>(ctClass.simpleName)
        }
        ctClass
    }
}

val jsonElementType = createTypeReference(JsonElement::class.java)
val jsonArrayType =  createTypeReference(JsonArray::class.java)
val jsonObjectType = createTypeReference(JsonObject::class.java)

fun addSerializeMethod(ctClass: CtClassImpl<Any>, objectSchema: ObjectSchema, classProvider: ClassProvider) {
    val method = CtMethodImpl<Any>().let {
        it.setVisibility<CtModifiable>(ModifierKind.PUBLIC)
        it.setType<CtTypedElement<Any>>(jsonObjectType)
        it.setSimpleName<CtMethod<Any>>("serialize")
        val statements = LinkedList<CtStatement>()
        statements.add(createLocalVar("res", jsonObjectType, objectInstance(jsonObjectType)))
        objectSchema.propertySchemas.forEach { statements.addAll(addSerializeStmts(it, classProvider)) }
        statements.add(returnStmt(localVarRef("res")))
        it.setBodyBlock(statements)
        it
    }
    ctClass.addMethod<Any, CtType<Any>>(method)
}

fun addSerializeStmts(entry: Map.Entry<String, Schema>, classProvider: ClassProvider): Collection<CtStatement> {
    return when (entry.value) {
        is StringSchema -> listOf(
                instanceMethodCall("addProperty", listOf(
                    stringLiteral(entry.key),
                    fieldRef(entry.key)
            ), target=localVarRef("res"))
        )
        is BooleanSchema -> listOf(
                instanceMethodCall("addProperty", listOf(
                        stringLiteral(entry.key),
                        fieldRef(entry.key)
                ), target=localVarRef("res"))
        )
        is ArraySchema -> listOf(
                createBlock(listOf(
                        createLocalVar("jsonArray", jsonArrayType, objectInstance(jsonArrayType)),
                        CtForEachImpl().let {
                            it.setVariable<CtForEach>(createLocalVar("element", (entry.value as ArraySchema).allItemSchema.toType(classProvider)))
                            it.setExpression<CtForEach>(fieldRef(entry.key))
                            it.setBody<CtBodyHolder>(createBlock(listOf(
                                instanceMethodCall("add", listOf(
                                        staticMethodCall("serialize", listOf(localVarRef("element")), createTypeReference("com.strumenta.json.SerializationUtils"))
                                ), target= localVarRef("jsonArray"))
                            )))
                            it
                        },
                        instanceMethodCall("addProperty", listOf(
                                stringLiteral(entry.key),
                                localVarRef("jsonArray")),
                                target=localVarRef("res"))
                ))
        )
        else -> throw UnsupportedOperationException("Unknown schema: ${entry.value.javaClass.canonicalName}")
    }
}

fun addUnserializeMethod(ctClass: CtClassImpl<Any>, objectSchema: ObjectSchema) {
    val method = CtMethodImpl<Any>().let {
        it.setType<CtTypedElement<Any>>(voidType())
        it.setModifiers<CtModifiable>(setOf(ModifierKind.STATIC, ModifierKind.PUBLIC))
        it.setSimpleName<CtMethod<Any>>("unserialize")
        it.setParameters<CtExecutable<Any>>(listOf(CtParameterImpl<Any>().let {
            it.setSimpleName<CtNamedElement>("json")
            it.setType<CtTypedElement<Any>>(jsonElementType)
            it
        }))
        val thisClass = createTypeReference(ctClass.qualifiedName)
        it.setBody<CtBodyHolder>(createBlock(listOf(
            createLocalVar("res", thisClass, objectInstance(thisClass)),
            returnStmt(localVarRef("res"))
        )))
        it
    }
    ctClass.addMethod<Any, CtType<Any>>(method)
}

fun generateClasses(schema: ObjectSchema, packageName: String, rootClassName: String) : List<CompilationUnit> {
    // First we create the classes
    val pack = CtPackageImpl()
    pack.setSimpleName<CtPackage>(packageName)

    val classProvider = ClassProvider(pack)
    schema.generateClassRecursively(classProvider, rootClassName)

    // Then we put them in compilation units and we generate them
    return classProvider.classesForObjectSchemas.map {
        val cu = CompilationUnitImpl()
        cu.isAutoImport = true
        cu.declaredPackage = pack
        cu.declaredTypes = listOf(it.value)

        cu
    }.toList()
}

class ClassProvider(val pack: CtPackage) {
    val classesForObjectSchemas = HashMap<ObjectSchema, CtClass<Any>>()

    fun register(objectSchema: ObjectSchema, ctClass: CtClass<Any>) {
        classesForObjectSchemas[objectSchema] = ctClass
    }

    fun typeReference(objectSchema: ObjectSchema): CtTypeReference<Any> {
        if (objectSchema !in classesForObjectSchemas) {
            classesForObjectSchemas[objectSchema] = objectSchema.generateClass(this)
        }
        return createTypeReference(classesForObjectSchemas[objectSchema]!!)
    }
}

fun generateJsonSchema(jsonSchema: InputStream, packageName: String, rootClassName: String) : List<GeneratedJavaFile> {
    val rawSchema = JSONObject(JSONTokener(jsonSchema))
    val schema = SchemaLoader.load(rawSchema) as ObjectSchema
    val cus = generateClasses(schema, packageName, rootClassName)

    val pp = DefaultJavaPrettyPrinter(StandardEnvironment())

    return cus.map { cu ->
        pp.calculate(cu, cu.declaredTypes)
        val filename = cu.declaredTypes[0].qualifiedName.replace('.', File.separatorChar) + ".java"

        GeneratedJavaFile(filename, pp.result)
    }
}

data class GeneratedJavaFile(val filename: String, val code: String)

fun main(args: Array<String>) {
    Dummy::class.java.getResourceAsStream("/a_json_schema.json").use {
        val generatedClasses = generateJsonSchema(it,
                "com.thefruit.company", "FruitThing")
        generatedClasses.forEach {
            println("*".repeat(it.filename.length))
            println(it.filename)
            println("*".repeat(it.filename.length))
            println(it.code)
        }
    }
}
