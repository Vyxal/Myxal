#ifndef JYXAL_STACK_HPP_INCLUDE
#define JYXAL_STACK_HPP_INCLUDE

#include <vector>
#include "types.hpp"

class MyxalStack {
    public:
        MyxalStack();

        std::vector<type>::iterator begin();
        std::vector<type>::iterator end();

        void push(type value);
        type pop();

    private:
        std::vector<type> stack;
};

#endif // JYXAL_STACK_HPP_INCLUDE