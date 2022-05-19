#ifndef JYXAL_STACK_HPP_INCLUDE
#define JYXAL_STACK_HPP_INCLUDE

#include <vector>
#include <string>
#include "types.hpp"

class MyxalStack {
    public:
        MyxalStack();

        std::vector<mtype>::iterator begin();
        std::vector<mtype>::iterator end();

        void push(mtype value);
        mtype pop();

        size_t size();

        std::string asString();

        list wrap();
        
        std::vector<mtype> stack;
};

#endif // JYXAL_STACK_HPP_INCLUDE