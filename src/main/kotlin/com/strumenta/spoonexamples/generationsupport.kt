package com.strumenta.spoonexamples

import spoon.reflect.code.*
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtNamedElement
import spoon.reflect.declaration.CtTypedElement
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtPackageReference
import spoon.reflect.reference.CtReference
import spoon.reflect.reference.CtTypeReference
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

fun staticMethodCall(methodName: String, params: List<CtExpression<Any>>, target: CtTypeReference<Any>) : CtInvocation<Any> {
    return CtInvocationImpl<Any>().let {
        it.setExecutable<CtAbstractInvocation<Any>>(
                CtExecutableReferenceImpl<Any>().let {
                    it.setDeclaringType<CtExecutableReference<Any>>(createTypeReference("ciao"))
                    it.setStatic<CtExecutableReference<Any>>(true)
                    it.setSimpleName<CtReference>(methodName)
                    it
                }
        )
        it.setArguments<CtAbstractInvocation<Any>>(params)
        it
    }
}

fun returnStmt(value: CtExpression<Any>?) : CtReturn<Any> {
    return CtReturnImpl<Any>().let {
        if (value != null) {
            it.setReturnedExpression<spoon.reflect.code.CtReturn<kotlin.Any>>(value)
        }
        it
    }
}
