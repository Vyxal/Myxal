package io.github.seggan.myxal.compiler.tree

import io.github.seggan.myxal.compiler.ICompiler

class VariableSetNode(val name: String) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.storeVariable(name)
    }
}

class VariableGetNode(val name: String) : Node {
    override fun accept(visitor: ICompiler) {
        visitor.loadVariable(name)
    }
}

class RegisterLoadNode : Node {
    override fun accept(visitor: ICompiler) {
        visitor.loadRegister()
    }
}

class RegisterSetNode : Node {
    override fun accept(visitor: ICompiler) {
        visitor.setRegister()
    }
}

