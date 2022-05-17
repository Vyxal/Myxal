#include "helpers.hpp"

bool truthValue(mtype value) {
    if (value->isNumber()) {
        number number = asNumber(value);
        if (number->isWhole()) {
            return number->asWhole() != 0;
        } else {
            return number->asDecimal() != 0;
        }
    } else if (value->isList()) {
        return !asList(value)->isEmpty();
    } else {
        return value->asString() != "";
    }
}