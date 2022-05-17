#ifndef GC_HPP_INCLUDE
#define GC_HPP_INCLUDE

#include <vector>
#include <memory>

class MyxalType;
class MyxalStack;

typedef std::shared_ptr<MyxalType> mtype;

void registerForGC(mtype type);

void runGC(MyxalStack &stack);

#endif // GC_HPP_INCLUDE