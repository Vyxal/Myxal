#ifndef JYXAL_STACK_HPP_INCLUDE
#define JYXAL_STACK_HPP_INCLUDE

#include <vector>
#include "types.hpp"

class MyxalStack {
    public:
        MyxalStack();

        std::vector<MyxalType *>::iterator begin();
        std::vector<MyxalType *>::iterator end();

        void push(MyxalType *value);
        MyxalType *pop();

    private:
        std::vector<MyxalType *> stack;
};

#endif // JYXAL_STACK_HPP_INCLUDE