#include "myxal_stack.hpp"

MyxalStack::MyxalStack() {
}

std::vector<type>::iterator MyxalStack::begin() {
    return stack.begin();
}

std::vector<type>::iterator MyxalStack::end() {
    return stack.end();
}

void MyxalStack::push(type value) {
    stack.push_back(value);
}

type MyxalStack::pop() {
    type ret = stack.back();
    stack.pop_back();
    return ret;
}