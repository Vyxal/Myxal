#ifndef GC_HPP_INCLUDE
#define GC_HPP_INCLUDE

#include <vector>

class MyxalType;

void registerForGC(MyxalType *type);

void runGC(std::vector<MyxalType *> &stack);

#endif // GC_HPP_INCLUDE