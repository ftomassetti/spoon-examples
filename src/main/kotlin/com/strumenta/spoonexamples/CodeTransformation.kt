package com.strumenta.spoonexamples

import spoon.Launcher
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.reference.CtParameterReference
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.visitor.CtAbstractVisitor
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.support.StandardEnvironment
import spoon.support.reflect.code.CtAssignmentImpl
import spoon.support.reflect.code.CtThisAccessImpl
import spoon.support.reflect.code.CtTypeAccessImpl
import spoon.support.reflect.cu.CompilationUnitImpl
import spoon.support.reflect.declaration.CtFieldImpl
import spoon.support.reflect.declaration.CtParameterImpl
import spoon.support.reflect.reference.CtTypeReferenceImpl

class ParamToFieldRefactoring(val paramName: String, val paramType: CtTypeReference<Any>) {

    fun refactor(clazz: CtClass<*>) {
        // Add field to the class
        clazz.addField<Any, Nothing>(CtFieldImpl<Any>().let {
            it.setSimpleName<CtNamedElement>(paramName)
            it.setType<CtTypedElement<Any>>(paramType)
            it
        })

        // Receive the value for the field in each constructor
        clazz.constructors.forEach {
            it.addParameter<Nothing>(CtParameterImpl<Any>().let {
                it.setSimpleName<CtNamedElement>(paramName)
                it.setType<CtTypedElement<Any>>(paramType)
                it
            })
            it.body.statements.add(CtAssignmentImpl<Any, Any>().let {
                it.setAssigned<CtAssignment<Any, Any>>(fieldRef(paramName).let {
                    val target = CtThisAccessImpl<Any>().let {
                        it.setTarget<CtTargetedExpression<Any, CtExpression<*>>>(CtTypeAccessImpl<Any>().let {
                            it.setAccessedType<CtTypeAccess<Any>>(createTypeReference(clazz.qualifiedName))
                            it
                        })
                        it
                    }
                    it.setTarget<CtTargetedExpression<Any, CtExpression<*>>>(target)
                    it
                })
                it.setAssignment<CtRHSReceiver<Any>>(localVarRef(paramName))
                it
            })
        }

        // TODO add constructor parameter and assignment
        clazz.methods.filter { findParamToChange(it) != null }.forEach {
            val param = findParamToChange(it)!!

            it.accept(object : CtAbstractVisitor() {
                override fun <T : Any?> visitCtParameterReference(reference: CtParameterReference<T>?) {
                    println("REFERENCE $reference")
                }
            })

            param.delete()
        }
    }

    fun findParamToChange(method: CtMethod<*>) : CtParameter<*>? {
        return method.parameters.find { it.simpleName == paramName }
    }
}

fun CtClass<*>.toCode() : String {
    val pp = DefaultJavaPrettyPrinter(StandardEnvironment())

    val cu = CompilationUnitImpl()

    pp.calculate(cu, listOf(this))
    return pp.result
}

fun main(args: Array<String>) {
    val originalCode = """class MyClass {
            MyClass() {
            }

            void foo(MyParam param, String otherParam) {
                param.doSomething();
            }

            int bar(MyParam param) {
                return param.count();
            }

        }"""
    val parsedClass = Launcher.parseClass(originalCode)
    ParamToFieldRefactoring("param", createTypeReference("com.strumenta.MyParam")).refactor(parsedClass)

    println(parsedClass.toCode())

}