#include "elements.hpp"

mtype decrement(mtype value) {
    if (value->isNumber()) {
        return asNumber(value)->sub(1);
    } else {
        return mt::mstring(value->asString() + "-");
    }
}