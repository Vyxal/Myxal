#include "myxal_stack.hpp"

MyxalStack::MyxalStack() {
}

std::vector<mtype>::iterator MyxalStack::begin() {
    return stack.begin();
}

std::vector<mtype>::iterator MyxalStack::end() {
    return stack.end();
}

void MyxalStack::push(mtype value) {
    stack.push_back(value);
}

mtype MyxalStack::pop() {
    mtype ret = stack.back();
    stack.pop_back();
    return ret;
}

size_t MyxalStack::size() {
    return stack.size();
}

std::string MyxalStack::asString() {
    std::string ret = "";
    std::vector<mtype>::iterator it = stack.begin();
    while (it != stack.end()) {
        ret += (*it)->asString();
        ret += " ";
        it++;
    }
    return ret;
}

list MyxalStack::wrap() {
    std::vector<mtype> l;
    for (auto it = stack.begin(); it != stack.end(); it++) {
        l.push_back(*it);
    }
    return mt::mlist(l);
}