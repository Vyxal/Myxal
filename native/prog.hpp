#ifndef PROG_HPP_INCLUDE
#define PROG_HPP_INCLUDE

#include <vector>
#include "types.hpp"
#include "myxal_stack.hpp"

MyxalStack &getStack();
void enterFunction();
void exitFunction();

MyxalType *&getContext();
void enterScope(MyxalType *context);
void exitScope();

void push(MyxalStack &stack = getStack());
MyxalType *pop(MyxalStack &stack = getStack());

#endif // PROG_HPP_INCLUDE