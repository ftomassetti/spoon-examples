import org.everit.json.schema.*
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.code.CtNewClass
import spoon.reflect.declaration.*
import spoon.reflect.reference.CtArrayTypeReference
import spoon.reflect.reference.CtPackageReference
import spoon.reflect.reference.CtReference
import spoon.reflect.reference.CtTypeReference
import spoon.support.reflect.declaration.CtClassImpl
import spoon.support.reflect.declaration.CtFieldImpl
import spoon.support.reflect.declaration.CtPackageImpl
import spoon.support.reflect.reference.CtArrayTypeReferenceImpl
import spoon.support.reflect.reference.CtPackageReferenceImpl
import spoon.support.reflect.reference.CtReferenceImpl
import spoon.support.reflect.reference.CtTypeReferenceImpl
import java.io.InputStream

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
        is ObjectSchema -> CtClassImpl<Any>().let {ctClass ->
            packag.types.add(ctClass)
            ctClass.setParent(packag)
            ctClass.setSimpleName<CtClass<Any>>(name ?: this.schemaLocation.split("/").last().capitalize())
            this.propertySchemas.forEach {
                ctClass.addProperty(packag, it.key, it.value)
            }
            CtTypeReferenceImpl<Any>().let {
                it.setPackage<CtTypeReferenceImpl<Any>>(ctClass.`package`.reference)
                it.setSimpleName<CtTypeReferenceImpl<Any>>(ctClass.simpleName)
            }
        }
        is ArraySchema -> CtArrayTypeReferenceImpl<Any>().let {
            it.setComponentType<CtArrayTypeReference<Any>>(this.allItemSchema.toType(packag))
        }
        is StringSchema -> CtTypeReferenceImpl<Any>().let {
            it.setSimpleName<CtTypeReference<*>>("String")
            it.setPackage<CtTypeReference<Any>>(CtPackageReferenceImpl().setTo("java.lang"))
        }
        is BooleanSchema -> CtTypeReferenceImpl<Any>().let {
            it.setSimpleName<CtTypeReference<*>>("Boolean")
            it.setPackage<CtTypeReference<Any>>(CtPackageReferenceImpl().setTo("java.lang"))
        }
        is ReferenceSchema -> referredSchema.toType(packag)
        else -> TODO("not implemented: ${this.javaClass}") //To change body of created functions use File | Settings | File Templates.
    }
}

private fun CtPackageReferenceImpl.setTo(packageName: String): CtPackageReferenceImpl {
    this.setSimpleName<CtPackageReference>(packageName)
    return this
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
//    launcher.addInputResource("codebases/jp/javaparser-core/src/main/java")
//    launcher.addInputResource("codebases/jp/javaparser-core-testing/src/test/java")
//    launcher.addInputResource("libs/junit-vintage-engine-4.12.3.jar")
//    launcher.environment.noClasspath = true
//    val model = launcher.buildModel()
//    val expressionClass = model.allTypes.filterIsInstance(CtClass::class.java).first {
//        it.qualifiedName == "com.github.javaparser.ast.expr.Expression"
//    }

    generateJsonSchema()
}
