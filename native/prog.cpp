#include <stack>
#include "prog.hpp"

std::stack<MyxalStack> stackStack;
std::stack<mtype> scopeStack;

MyxalStack &getStack() {
    return stackStack.top();
}

void enterFunction() {
    stackStack.push(MyxalStack());
}

void exitFunction() {
    stackStack.pop();
}

mtype& context() {
    return scopeStack.top();
}

void enterScope(mtype context) {
    scopeStack.push(context);
}

void exitScope() {
    scopeStack.pop();
}

void push(mtype value, MyxalStack &stack) {
    stack.push(value);
}

mtype pop(MyxalStack &stack) {
    return stack.pop();
} 