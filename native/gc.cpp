#include "gc.hpp"
#include "types.hpp"
#include "prog.hpp"

std::vector<std::weak_ptr<MyxalType>> registeredObjects;
int registerCounter = 0;

void registerForGC(mtype type) {
    registeredObjects.push_back(type);
    if (++registerCounter > 1000) {
        runGC(getStack());
        registerCounter = 0;
    }
}

void runGC(MyxalStack &stack) {
    for (auto &item : stack) {
        item->mark();
    }
    std::vector<std::weak_ptr<MyxalType>> newVector;
    for (auto &item : registeredObjects) {
        if (!item.expired()) {
            mtype type = item.lock();
            if (type->marked) {
                type->marked = false;
                newVector.push_back(item);
            } else {
                if (type->isList()) {
                    asList(type)->freeAllReferences();
                }
            }
        }
    }
    registeredObjects = newVector;
}