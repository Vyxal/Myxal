#ifndef PROG_HPP_INCLUDE
#define PROG_HPP_INCLUDE

#include <vector>
#include "types.hpp"

std::vector<MyxalType *> &getStack();

MyxalType *&getContext();
void enterScope(MyxalType *context);
void exitScope();

#endif // PROG_HPP_INCLUDE