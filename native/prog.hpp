#ifndef PROG_HPP_INCLUDE
#define PROG_HPP_INCLUDE

#include <vector>
#include "types.hpp"
#include "myxal_stack.hpp"

MyxalStack &getStack();
void enterFunction();
void exitFunction();

mtype &context();
void enterScope(mtype context);
void exitScope();

void push(mtype value, MyxalStack &stack = getStack());
mtype pop(MyxalStack &stack = getStack());

mtype &mregister();

#endif // PROG_HPP_INCLUDE