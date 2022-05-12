#include "jyxal_stack.hpp"

MyxalStack::MyxalStack() {
}

std::vector<MyxalType *>::iterator MyxalStack::begin() {
    return stack.begin();
}

std::vector<MyxalType *>::iterator MyxalStack::end() {
    return stack.end();
}

void MyxalStack::push(MyxalType *value) {
    stack.push_back(value);
}

MyxalType *MyxalStack::pop() {
    MyxalType *ret = stack.back();
    stack.pop_back();
    return ret;
}