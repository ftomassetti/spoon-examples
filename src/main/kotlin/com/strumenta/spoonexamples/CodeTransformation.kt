package com.strumenta.spoonexamples

import spoon.Launcher
import spoon.reflect.code.*
import spoon.reflect.declaration.*
import spoon.reflect.reference.*
import spoon.reflect.visitor.CtAbstractVisitor
import spoon.reflect.visitor.CtIterator
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.support.StandardEnvironment
import spoon.support.reflect.code.CtAssignmentImpl
import spoon.support.reflect.code.CtThisAccessImpl
import spoon.support.reflect.code.CtTypeAccessImpl
import spoon.support.reflect.cu.CompilationUnitImpl
import spoon.support.reflect.declaration.CtFieldImpl
import spoon.support.reflect.declaration.CtParameterImpl
import spoon.support.reflect.reference.CtFieldReferenceImpl
import spoon.support.reflect.reference.CtTypeReferenceImpl

fun qualifiedFieldAccess(name: String, className: String) : CtFieldAccess<Any> {
    return fieldRef(name).let {
        val target = CtThisAccessImpl<Any>().let {
            it.setTarget<CtTargetedExpression<Any, CtExpression<*>>>(CtTypeAccessImpl<Any>().let {
                it.setAccessedType<CtTypeAccess<Any>>(createTypeReference(className))
                it
            })
            it
        }
        it.setTarget<CtTargetedExpression<Any, CtExpression<*>>>(target)
        it
    }
}

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
                it.setAssigned<CtAssignment<Any, Any>>(qualifiedFieldAccess(paramName, clazz.qualifiedName))
                it.setAssignment<CtRHSReceiver<Any>>(localVarRef(paramName))
                it
            })
        }

        clazz.methods.filter { findParamToChange(it) != null }.forEach {
            val param = findParamToChange(it)!!

            CtIterator(it).forEach {
                if (it is CtParameterReference<*> && it.simpleName == paramName) {
                    val cfr = CtFieldReferenceImpl<Any>()
                    cfr.setSimpleName<CtReference>(paramName)
                    cfr.setDeclaringType<CtFieldReference<Any>>(createTypeReference(clazz.qualifiedName))
                    it.replace(cfr)
                }
            }

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