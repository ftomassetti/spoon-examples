package com.strumenta.spoonexamples

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.everit.json.schema.*
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.reference.CtPackageReference
import spoon.reflect.reference.CtReference
import spoon.reflect.reference.CtTypeReference
import spoon.support.reflect.code.*
import spoon.support.reflect.declaration.*
import spoon.support.reflect.reference.CtExecutableReferenceImpl
import spoon.support.reflect.reference.CtLocalVariableReferenceImpl
import spoon.support.reflect.reference.CtPackageReferenceImpl
import spoon.support.reflect.reference.CtTypeReferenceImpl
import java.lang.UnsupportedOperationException
import java.util.*

class Dummy

fun CtClass<*>.addProperty(packag: CtPackage, name: String, schema: Schema) {
    val field = CtFieldImpl<Any>().let {
        it.setSimpleName<CtField<Any>>(name)
        it.setType<CtField<Any>>(schema.toType(packag))
        it.setVisibility<CtField<Any>>(ModifierKind.PRIVATE)
    }
    this.addField<Any, Nothing>(field)
}

private fun Schema.toType(packag: CtPackage, name: String? = null): CtTypeReference<Any> {
    return when (this) {
        is ObjectSchema -> CtClassImpl<Any>().let { ctClass ->
            packag.types.add(ctClass)
            ctClass.setParent(packag)
            ctClass.setVisibility<CtModifiable>(ModifierKind.PUBLIC)
            ctClass.setSimpleName<CtClass<Any>>(name ?: this.schemaLocation.split("/").last().capitalize())
            this.propertySchemas.forEach {
                ctClass.addProperty(packag, it.key, it.value)
            }
            addUnserializeMethod(ctClass, this)
            addSerializeMethod(ctClass, this, packag, name)
            CtTypeReferenceImpl<Any>().let {
                it.setPackage<CtTypeReferenceImpl<Any>>(ctClass.`package`.reference)
                it.setSimpleName<CtTypeReferenceImpl<Any>>(ctClass.simpleName)
            }
        }
        is ArraySchema -> createTypeReference(List::class.java).let {
            it.setActualTypeArguments<CtTypeReference<Any>>(listOf(this.allItemSchema.toType(packag)))
            it
        }
        is StringSchema -> createTypeReference(String::class.java)
        is BooleanSchema -> createTypeReference(Boolean::class.java)
        is ReferenceSchema -> referredSchema.toType(packag)
        else -> TODO("not implemented: ${this.javaClass}") //To change body of created functions use File | Settings | File Templates.
    }
}



fun jsonElementType() =  createTypeReference(JsonElement::class.java)
fun jsonArrayType() =  createTypeReference(JsonArray::class.java)
fun jsonObjectType() = createTypeReference(JsonObject::class.java)

fun addSerializeMethod(ctClass: CtClassImpl<Any>, objectSchema: ObjectSchema, packag: CtPackage, name: String? = null) {
    val method = CtMethodImpl<Any>().let {
        it.setVisibility<CtModifiable>(ModifierKind.PUBLIC)
        it.setType<CtTypedElement<Any>>(jsonObjectType())
        it.setSimpleName<CtMethod<Any>>("serialize")
        val statements = LinkedList<CtStatement>()
        statements.add(createLocalVar("res", jsonObjectType(), objectInstance(jsonObjectType())))
        objectSchema.propertySchemas.forEach { statements.addAll(addSerializeStmts(it, packag, name)) }
        statements.add(returnStmt(localVarRef("res")))
        it.setBodyBlock(statements)
        it
    }
    ctClass.addMethod<Any, CtType<Any>>(method)
}

fun addSerializeStmts(entry: Map.Entry<String, Schema>, packag: CtPackage, name: String? = null): Collection<CtStatement> {
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
                        createLocalVar("jsonArray", jsonArrayType(), objectInstance(jsonArrayType())),
                        CtForEachImpl().let {
                            it.setVariable<CtForEach>(createLocalVar("element", (entry.value as ArraySchema).allItemSchema.toType(packag, name)))
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
            it.setType<CtTypedElement<Any>>(jsonElementType())
            it
        }))
        it
    }
    ctClass.addMethod<Any, CtType<Any>>(method)
}

fun generateClasses(schema: ObjectSchema, packageName: String, rootClassName: String) {
    val pack = CtPackageImpl()
    pack.setSimpleName<CtPackage>(packageName)

    schema.toType(pack, rootClassName)

    pack.types.forEach {
        println(it)
    }
}

fun generateJsonSchema() {
    Dummy::class.java.getResourceAsStream("/a_json_schema.json").use {
        val rawSchema = JSONObject(JSONTokener(it))
        val schema = SchemaLoader.load(rawSchema)
        generateClasses(schema as ObjectSchema, "com.thefruit.company", "FruitThing")
    }
}

fun main(args: Array<String>) {
//    val launcher = Launcher()
//    launcher.addInputResource("codebases/jp/javaparser-core/src/com.strumenta.spoonexamples.main/java")
//    launcher.addInputResource("codebases/jp/javaparser-core-testing/src/test/java")
//    launcher.addInputResource("libs/junit-vintage-engine-4.12.3.jar")
//    launcher.environment.noClasspath = true
//    val model = launcher.buildModel()
//    val expressionClass = model.allTypes.filterIsInstance(CtClass::class.java).first {
//        it.qualifiedName == "com.github.javaparser.ast.expr.Expression"
//    }

    generateJsonSchema()
}
