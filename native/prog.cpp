#include <stack>
#include "prog.hpp"

std::stack<MyxalStack> stackStack;
std::stack<MyxalType *> scopeStack;

MyxalStack &getStack() {
    return stackStack.top();
}

void enterFunction() {
    stackStack.push(MyxalStack());
}

void exitFunction() {
    stackStack.pop();
}

MyxalType *&getContext() {
    return scopeStack.top();
}

void enterScope(MyxalType *context) {
    scopeStack.push(context);
}

void exitScope() {
    scopeStack.pop();
}

void push(MyxalType *value, MyxalStack &stack) {
    stack.push(value);
}

MyxalType *pop(MyxalStack &stack) {
    return stack.pop();
}