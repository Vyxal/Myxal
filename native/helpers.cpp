#include "helpers.hpp"

MyxalList::iterator iterator(mtype value) {
    if (value->isList()) {
        return asList(value)->begin();
    } else {
        std::vector<mtype> values;
        std::string str = value->asString();
        for (int i = 0; i < str.length(); i++) {
            values.push_back(mt::mstring(std::string(1, str[i])));
        }
        return mt::mlist(values)->begin();
    }
}

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