#include "types.hpp"

bool MyxalType::isList() {
    return false;
}

bool MyxalType::isNumber() {
    return false;
}

bool MyxalType::isString() {
    return false;
}

void MyxalType::mark(bool mark) {
    marked = mark;
}

MyxalNumber *MyxalType::asNumber() {
    MyxalNumber *number = dynamic_cast<MyxalNumber *>(this);
    if (number) {
        return number;
    } else {
        throw "Not a number";
    }
}

MyxalList *MyxalType::asList() {
    MyxalList *list = dynamic_cast<MyxalList *>(this);
    if (list) {
        return list;
    } else {
        throw "Not a list";
    }
}

MyxalNumber::MyxalNumber(long long value) {
    this->value = value;
    this->isWholeNumber = true;
    this->decimal = 0;
    registerForGC(this);
}

MyxalNumber::MyxalNumber(long double value) {
    this->value = 0;
    this->isWholeNumber = false;
    this->decimal = value;
    registerForGC(this);
}

MyxalNumber::MyxalNumber(MyxalNumber &other) {
    this->value = other.value;
    this->isWholeNumber = other.isWholeNumber;
    this->decimal = other.decimal;
    registerForGC(this);
}

long long MyxalNumber::asWhole() {
    if (isWholeNumber) {
        return value;
    } else {
        return decimal;
    }
}

long double MyxalNumber::asDecimal() {
    if (isWholeNumber) {
        return value;
    } else {
        return decimal;
    }
}

bool MyxalNumber::isWhole() {
    return isWholeNumber;
}

bool MyxalNumber::isNumber() {
    return true;
}

std::string MyxalNumber::asString() {
    if (isWholeNumber) {
        return std::to_string(value);
    } else {
        return std::to_string(decimal);
    }
}

MyxalNumber *MyxalNumber::operator+(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return new MyxalNumber(this->value + other.value);
    } else {
        // The rest are gonna end up as decimals
        return new MyxalNumber(this->asDecimal() + other.asDecimal());
    }
}

MyxalNumber *MyxalNumber::operator-(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return new MyxalNumber(this->value - other.value);
    } else {
        // The rest are gonna end up as decimals
        return new MyxalNumber(this->asDecimal() - other.asDecimal());
    }
}

MyxalNumber *MyxalNumber::operator*(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return new MyxalNumber(this->value * other.value);
    } else {
        // The rest are gonna end up as decimals
        return new MyxalNumber(this->asDecimal() * other.asDecimal());
    }
}

MyxalNumber *MyxalNumber::operator/(MyxalNumber &other) {
    // We want float division here
    return new MyxalNumber(this->asDecimal() / other.asDecimal());
}

MyxalNumber *MyxalNumber::operator%(MyxalNumber &other) {
    // Mod only works on integers
    return new MyxalNumber(this->asWhole() % other.asWhole());
}


// List
MyxalList::MyxalList() {
    registerForGC(this);
    this->generator = [] { return nullptr; };
    this->isDone = true;
}

MyxalList::MyxalList(MyxalList &other) : MyxalList(other.size()) {
    for (auto &item : other.backing) {
        this->backing.push_back(item);
    }
    this->generator = other.generator;
    this->isDone = other.isDone;
}

MyxalList::MyxalList(std::vector<MyxalType *> values) {
    this->backing = backing;
    this->generator = [] { return nullptr; };
    this->isDone = true;
    registerForGC(this);
}

MyxalList::MyxalList(std::function<MyxalType *()> generator) {
    this->backing = std::vector<MyxalType *>();
    this->generator = generator;
    this->isDone = false;
    registerForGC(this);
}

MyxalList::MyxalList(size_t size) {
    this->backing.reserve(size);
    this->generator = [] { return nullptr; };
    this->isDone = true;
    registerForGC(this);
}

size_t MyxalList::size() {
    if (isDone) {
        return backing.size();
    } else {
        MyxalType *item;
        while ((item = generator()) != nullptr) {
            backing.push_back(item);
        }
        isDone = true;
        return backing.size();
    }
}

bool MyxalList::isList() {
    return true;
}

bool MyxalList::hasIndex(size_t index) {
    fill(index);
    return index < backing.size();
}

MyxalType *MyxalList::operator[](int index) {
    fill(index);
    return backing[index];
}

std::string MyxalList::asString() {
    std::string result = "[";
    for (auto &item : backing) {
        result += item->asString();
        result += ", ";
    }
    if (backing.size() > 0) {
        result.pop_back();
        result.pop_back();
    }
    result += "]";
    return result;
}

MyxalList *MyxalList::operator+(MyxalList &other) {
    MyxalList *result = new MyxalList(this->backing.size() + other.backing.size());
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    for (auto &item : other.backing) {
        result->backing.push_back(item);
    }
    return result;
}

MyxalList *MyxalList::operator+(MyxalType *other) {
    MyxalList *result = new MyxalList(this->backing.size() + 1);
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    result->backing.push_back(other);
    return result;
}

MyxalList *MyxalList::map(std::function<MyxalType *(MyxalType *)> func) {
    size_t index = 0;
    MyxalList *result = new MyxalList([&] () -> MyxalType * {
        if (!hasIndex(index)) {
            return nullptr;
        }
        return func(backing[index++]);
    });
    result->depend = this;
    return result;
}

void MyxalList::fill(size_t targetSize) {
    if (!isDone) {
        MyxalType *item;
        while (backing.size() <= targetSize) {
            if ((item = generator()) == nullptr) {
                isDone = true;
                break;
            }
            backing.push_back(item);
        }
    }
}

MyxalList::iterator::iterator(MyxalList *list, size_t index) {
    this->list = list;
    this->index = index;
}

MyxalType *MyxalList::iterator::operator*() {
    return list->backing[index];
}

MyxalList::iterator &MyxalList::iterator::operator++() {
    index++;
    return *this;
}

bool MyxalList::iterator::hasNext() {
    return list->hasIndex(index + 1);
}

bool MyxalList::iterator::operator!=(MyxalList::iterator &other) {
    return index != other.index;
}

MyxalList::iterator MyxalList::begin() {
    return MyxalList::iterator(this, 0);
}

void MyxalList::mark(bool mark) {
    marked = mark;
    for (auto &item : backing) {
        item->mark(mark);
    }
    depend->mark(mark);
}