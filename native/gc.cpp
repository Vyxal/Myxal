#include "gc.hpp"
#include "types.hpp"
#include "prog.hpp"

std::vector<type> registeredObjects;
int registerCounter = 0;

void registerForGC(type type) {
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
    std::vector<type> newVector;
    for (auto &item : registeredObjects) {
        if (item->marked) {
            newVector.push_back(item);
            item->marked = false;
        } else {
            item.reset();
        }
    }
    registeredObjects = newVector;
}