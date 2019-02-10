package com.strumenta.spoonexamples

import com.sun.org.apache.xpath.internal.operations.Bool
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.factory.FactoryImpl
import spoon.reflect.reference.CtPackageReference
import spoon.reflect.reference.CtReference
import spoon.reflect.reference.CtTypeReference
import spoon.support.DefaultCoreFactory
import spoon.support.StandardEnvironment
import spoon.support.reflect.code.*
import spoon.support.reflect.declaration.CtExecutableImpl
import spoon.support.reflect.reference.CtExecutableReferenceImpl
import spoon.support.reflect.reference.CtPackageReferenceImpl
import spoon.support.reflect.reference.CtTypeReferenceImpl


fun CtPackageReferenceImpl.setTo(packageName: String): CtPackageReferenceImpl {
    this.setSimpleName<CtPackageReference>(packageName)
    return this
}

fun createTypeReference(`class`: Class<*>) = createTypeReference(`class`.canonicalName)

fun createTypeReference(ctClass: CtClass<Any>) = createTypeReference(ctClass.qualifiedName)

fun createTypeReference(canonicalName: String) : CtTypeReference<Any> {
    val pos = canonicalName.indexOfLast { it == '.' }
    return CtTypeReferenceImpl<Any>().let {
        it.factory = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
        if (pos == -1) {
            it.setSimpleName<CtTypeReference<*>>(canonicalName)
        } else {
            it.setSimpleName<CtTypeReference<*>>(canonicalName.substring(pos + 1))
            it.setPackage<CtTypeReference<Any>>(CtPackageReferenceImpl().setTo(canonicalName.substring(0, pos)))
        }
        it
    }
}

fun voidType() = CtTypeReferenceImpl<Any>().let {
    it.setSimpleName<CtTypeReferenceImpl<Any>>("void")
    it
}

fun <R> CtExecutableImpl<R>.setBodyBlock(vararg statements: CtStatement) {
    this.setBody<CtBodyHolder>(createBlock(statements.toList()))
}

fun <R> CtExecutableImpl<R>.setBodyBlock(statements: List<CtStatement>) {
    this.setBody<CtBodyHolder>(createBlock(statements))
}

fun createBlock(statements: List<CtStatement>) : CtBlock<Any> {
    return CtBlockImpl<Any>().let { block ->
        statements.forEach { block.addStatement<CtStatementList>(it) }
        block
    }
}

fun createLocalVar(name: String, type: CtTypeReference<Any>, value: CtExpression<Any>? = null) : CtLocalVariable<Any> {
    return CtLocalVariableImpl<Any>().let {
        it.setSimpleName<CtNamedElement>(name)
        it.setType<CtTypedElement<Any>>(type)
        if (value != null) {
            it.setAssignment<CtRHSReceiver<Any>>(value)
        }
        it
    }
}

fun objectInstance(type: CtTypeReference<Any>) : CtConstructorCall<Any> {
    return CtConstructorCallImpl<Any>().let {
        it.setType<CtTypedElement<Any>>(type)
        it
    }
}

fun localVarRef(name: String) : CtVariableRead<Any> {
    return CtVariableReadImpl<kotlin.Any>().let {
        it.setVariable<spoon.reflect.code.CtVariableAccess<kotlin.Any>>(spoon.support.reflect.reference.CtLocalVariableReferenceImpl<kotlin.Any>().let {
            it.setSimpleName<spoon.reflect.reference.CtReference>(name)
            it
        })
        it
    }
}

fun fieldRef(name: String) : CtFieldAccess<Any> {
    return CtFieldReadImpl<kotlin.Any>().let {
        it.setVariable<spoon.reflect.code.CtVariableAccess<kotlin.Any>>(spoon.support.reflect.reference.CtFieldReferenceImpl<kotlin.Any>().let {
            it.setSimpleName<spoon.reflect.reference.CtReference>(name)
            it
        })
        it
    }
}

fun stringLiteral(value: String) : CtLiteral<Any> = CtLiteralImpl<Any>().let {
    it.setValue<CtLiteral<Any>>(value)
    it
}

fun instanceMethodCall(methodName: String, params: List<CtExpression<Any>>, target: CtExpression<Any>? = null) : CtInvocation<Any> {
    return CtInvocationImpl<Any>().let {
        if (target != null) {
            it.setTarget<CtTargetedExpression<Any, CtExpression<*>>>(target)
        }
        it.setExecutable<CtAbstractInvocation<Any>>(
                CtExecutableReferenceImpl<Any>().let {
                    it.setSimpleName<CtReference>(methodName)
                    it
                }
        )
        it.setArguments<CtAbstractInvocation<Any>>(params)
        it
    }
}

fun staticMethodCall(methodName: String, params: List<CtExpression<Any>>, target: CtTypeReference<Any>) : CtExpression<Any> {
    val f = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
    return f.createCodeSnippetExpression("$target.$methodName(${params.joinToString(separator = ", ")})")
}

fun returnStmt(value: CtExpression<Any>?) : CtReturn<Any> {
    return CtReturnImpl<Any>().let {
        if (value != null) {
            it.setReturnedExpression<spoon.reflect.code.CtReturn<kotlin.Any>>(value)
        }
        it
    }
}

fun classField(cl: Class<*>) : CtCodeSnippetExpression<Any> {
    val f = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
    return f.createCodeSnippetExpression("${cl.canonicalName}.class")
}

fun classField(cl: CtTypeReference<*>) : CtCodeSnippetExpression<Any> {
    val f = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
    return f.createCodeSnippetExpression("${cl.qualifiedName}.class")
}

fun cast(expression: CtExpression<Any>, cl: CtTypeReference<Any>) : CtExpression<Any> {
    val f = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
    return f.createCodeSnippetExpression("(${cl.qualifiedName}) ${expression.toString()}")
}

fun addGetter(cl: CtClass<*>, field: CtField<*>) {
    val f = cl.factory
    val body = f.createBlock<Any>()
    body.addStatement<CtStatementList>(f.createCodeSnippetStatement("return " + field.simpleName))

    f.Method().create<Any, Any>(cl, //Hosting class
            setOf(ModifierKind.PUBLIC), //Modifiers
            field.type as CtTypeReference<Any>?, //return type
            "get" + field.simpleName.capitalize(), // Name
            listOf(), // Parameters
            setOf(), // Throwable exceptions
            body //body
    )
}

fun addSetter(cl: CtClass<*>, field: CtField<*>) {
    val f = cl.factory
    val body = f.createBlock<Void>()
    body.addStatement<CtStatementList>(f.createCodeSnippetStatement("this." + field.simpleName + " = " + field.simpleName))

    //Executable reference will be filled later
    val p = f.createParameter(null, field.type, field.simpleName)

    f.Method().create<Void, Void>(cl, //Hosting class
            setOf(ModifierKind.PUBLIC), //Modifiers
            f.Type().VOID_PRIMITIVE, //return type
            "set" + field.simpleName.capitalize(), // Name
            listOf(p), // Parameters
            setOf(), // Throwable exceptions
            body //body
    )
}
