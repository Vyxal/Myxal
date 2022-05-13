#include "types.hpp"

bool truthValue(type value) {
    if (value->isNumber()) {
        number number = value->asNumber();
        if (number->isWhole()) {
            return number->asWhole() != 0;
        } else {
            return number->asDecimal() != 0;
        }
    } else if (value->isList()) {
        return !value->asList()->isEmpty();
    } else {
        return value->asString() != "";
    }
}