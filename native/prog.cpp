#include <stack>
#include "prog.hpp"

std::vector<MyxalType *> stack;
std::stack<MyxalType *> scopeStack;

std::vector<MyxalType *> &getStack() {
    return stack;
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