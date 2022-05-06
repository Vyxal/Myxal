package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class VariableSetNode(val name: String) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.storeVariable(name)
    }
    
    override fun toString(): String {
        return "(set '$name')"
    }
}

class VariableGetNode(val name: String) : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.loadVariable(name)
    }
    
    override fun toString(): String {
        return "(get '$name')"
    }
}

class RegisterLoadNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.loadRegister()
    }
    
    override fun toString(): String {
        return "(registerLoad)"
    }
}

class RegisterSetNode : Node {
    override fun accept(visitor: ICompiler<*>) {
        visitor.setRegister()
    }
    
    override fun toString(): String {
        return "(registerSet)"
    }
}

