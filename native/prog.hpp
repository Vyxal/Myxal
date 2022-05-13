#ifndef PROG_HPP_INCLUDE
#define PROG_HPP_INCLUDE

#include <vector>
#include "types.hpp"
#include "myxal_stack.hpp"

MyxalStack &getStack();
void enterFunction();
void exitFunction();

type &getContext();
void enterScope(type context);
void exitScope();

void push(MyxalStack &stack = getStack());
type pop(MyxalStack &stack = getStack());

#endif // PROG_HPP_INCLUDE