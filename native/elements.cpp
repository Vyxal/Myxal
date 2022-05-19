#include <cctype>
#include <cmath>
#include <algorithm>

#include "helpers.hpp"
#include "elements.hpp"

mtype decrement(mtype value) {
    if (value->isNumber()) {
        return asNumber(value)->sub(1);
    } else {
        return mt::mstring(value->asString() + "-");
    }
}

mtype increment(mtype value) {
    if (value->isNumber()) {
        return asNumber(value)->add(1);
    } else {
        std::string str = value->asString();
        std::replace(str.begin(), str.end(), ' ', '0');
        return mt::mstring(str);
    }
}

mtype isPrime(mtype value) {
    if (value->isNumber()) {
        long long val = asNumber(value)->asWhole();
        if (val < 2) {
            return mt::mfalse();
        }
        if (val == 2 || val == 3 || val == 5) {
            return mt::mtrue();
        }
        if (val % 2 == 0 || val % 3 == 0 || val % 5 == 0) {
            return mt::mfalse();
        }
        long long root = sqrt(val);
        int step = 4;
        for (long long i = 6; i <= root; i += step, step = 6 - step) {
            if (val % i == 0) {
                return mt::mfalse();
            }
        }
        return mt::mtrue();
    } else {
        std::string str = value->asString();
        if (str.length() == 0) {
            return mt::mfalse();
        }
        bool isUpper = isupper(str[0]);
        for (int i = 0; i < str.length(); i++) {
            if (isupper(str[i]) != isUpper) {
                return mt::mnumber(-1);
            }
        }
        return isUpper ? mt::mtrue() : mt::mfalse();
    }
}

mtype joinByNewlines(mtype value) {
    std::string result = "";
    MyxalList::iterator it = iterator(value);
    while (it.hasNext()) {
        result += (*it)->asString() + "\n";
        ++it;
    }
    return mt::mstring(result);
}