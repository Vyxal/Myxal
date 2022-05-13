#include <stack>
#include "prog.hpp"

std::stack<MyxalStack> stackStack;
std::stack<type> scopeStack;

MyxalStack &getStack() {
    return stackStack.top();
}

void enterFunction() {
    stackStack.push(MyxalStack());
}

void exitFunction() {
    stackStack.pop();
}

type& getContext() {
    return scopeStack.top();
}

void enterScope(type context) {
    scopeStack.push(context);
}

void exitScope() {
    scopeStack.pop();
}

void push(type value, MyxalStack &stack) {
    stack.push(value);
}

type pop(MyxalStack &stack) {
    return stack.pop();
}