#ifndef GC_HPP_INCLUDE
#define GC_HPP_INCLUDE

#include <vector>
#include <memory>
#include "myxal_stack.hpp"

class MyxalType;

typedef std::shared_ptr<MyxalType> type;

void registerForGC(type type);

void runGC(MyxalStack &stack);

#endif // GC_HPP_INCLUDE