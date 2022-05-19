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

MyxalNumber::MyxalNumber(int value) : MyxalNumber(static_cast<long long>(value)) {
}

MyxalNumber::MyxalNumber(long long value) {
    this->value = value;
    this->isWholeNumber = true;
    this->decimal = 0;
}

MyxalNumber::MyxalNumber(double value) : MyxalNumber(static_cast<long double>(value)) {
}

MyxalNumber::MyxalNumber(long double value) {
    this->value = 0;
    this->isWholeNumber = false;
    this->decimal = value;
}

MyxalNumber::MyxalNumber(bool value) : MyxalNumber(value ? 1 : 0) {
}

MyxalNumber::MyxalNumber(MyxalNumber &other) {
    this->value = other.value;
    this->isWholeNumber = other.isWholeNumber;
    this->decimal = other.decimal;
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

number MyxalNumber::add(MyxalNumber other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return mt::mnumber(this->value + other.value);
    } else {
        // The rest are gonna end up as decimals
        return mt::mnumber(this->asDecimal() + other.asDecimal());
    }
}

number MyxalNumber::sub(MyxalNumber other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return mt::mnumber(this->value - other.value);
    } else {
        // The rest are gonna end up as decimals
        return mt::mnumber(this->asDecimal() - other.asDecimal());
    }
}

number MyxalNumber::mul(MyxalNumber other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return mt::mnumber(this->value * other.value);
    } else {
        // The rest are gonna end up as decimals
        return mt::mnumber(this->asDecimal() * other.asDecimal());
    }
}

number MyxalNumber::div(MyxalNumber other) {
    // We want float division here
    return mt::mnumber(this->asDecimal() / other.asDecimal());
}

number MyxalNumber::mod(MyxalNumber other) {
    // Mod only works on integers
    return mt::mnumber(this->asWhole() % other.asWhole());
}


// List
MyxalList::MyxalList() {
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

MyxalList::MyxalList(std::vector<mtype> values) {
    this->backing = backing;
    this->generator = [] { return nullptr; };
    this->isDone = true;
}

MyxalList::MyxalList(std::function<mtype()> generator) {
    this->backing = std::vector<mtype>();
    this->generator = generator;
    this->isDone = false;
}

MyxalList::MyxalList(size_t size) {
    this->backing.reserve(size);
    this->generator = [] { return nullptr; };
    this->isDone = true;
}

size_t MyxalList::size() {
    if (isDone) {
        return backing.size();
    } else {
        mtype item;
        while ((item = generator()) != nullptr) {
            backing.push_back(item);
        }
        isDone = true;
        return backing.size();
    }
}

bool MyxalList::isEmpty() {
    return backing.empty() && isDone;
}

bool MyxalList::isList() {
    return true;
}

bool MyxalList::hasIndex(size_t index) {
    fill(index);
    return index < backing.size();
}

mtype MyxalList::operator[](int index) {
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

list MyxalList::operator+(MyxalList &other) {
    list result = std::make_shared<MyxalList>(this->backing.size() + other.backing.size());
    registerForGC(result);
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    for (auto &item : other.backing) {
        result->backing.push_back(item);
    }
    return result;
}

list MyxalList::operator+(mtype other) {
    list result = std::make_shared<MyxalList>(this->backing.size() + 1);
    registerForGC(result);
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    result->backing.push_back(other);
    return result;
}

list MyxalList::map(std::function<mtype(mtype)> func) {
    size_t index = 0;
    list result = mt::mlist([&] () -> mtype {
        if (!hasIndex(index)) {
            return nullptr;
        }
        return func(backing[index++]);
    });
    result->depend = asList(shared_from_this());
    return result;
}

void MyxalList::fill(size_t targetSize) {
    if (!isDone) {
        mtype item;
        while (backing.size() <= targetSize) {
            if ((item = generator()) == nullptr) {
                isDone = true;
                break;
            }
            backing.push_back(item);
        }
    }
}

MyxalList::iterator::iterator(std::shared_ptr<MyxalList> list, size_t index) {
    this->list = list;
    this->index = index;
}

mtype MyxalList::iterator::operator*() {
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
    return MyxalList::iterator(asList(shared_from_this()), 0);
}

void MyxalList::mark(bool mark) {
    marked = mark;
    for (auto &item : backing) {
        item->mark(mark);
    }
    depend->mark(mark);
}

void MyxalList::freeAllReferences() {
    for (auto &item : backing) {
        item.reset();
    }
    depend.reset();
}

MyxalString::MyxalString(std::string value) {
    this->value = value;
}

MyxalString::MyxalString(MyxalString &other) {
    this->value = other.value;
}

bool MyxalString::isString() {
    return true;
}

std::string MyxalString::asString() {
    return value;
}

list asList(mtype item) {
    if (item->isList()) {
        return std::static_pointer_cast<MyxalList>(item);
    } else {
        throw "Not a list: " + item->asString();
    }
}

number asNumber(mtype item) {
    if (item->isNumber()) {
        return std::static_pointer_cast<MyxalNumber>(item);
    } else {
        throw "Not a number: " + item->asString();
    }
}

string asString(mtype item) {
    if (item->isString()) {
        return std::static_pointer_cast<MyxalString>(item);
    } else {
        throw "Not a string: " + item->asString();
    }
}

number mt::mnumber(int num) {
    number created = std::make_shared<MyxalNumber>(num);
    registerForGC(created);
    return created;
}

number mt::mnumber(double num) {
    number created = std::make_shared<MyxalNumber>(num);
    registerForGC(created);
    return created;
}

number mt::mnumber(long double num) {
    number created = std::make_shared<MyxalNumber>(num);
    registerForGC(created);
    return created;
}

number mt::mnumber(long long num) {
    number created = std::make_shared<MyxalNumber>(num);
    registerForGC(created);
    return created;
}

number mt::mfalse() {
    number created = std::make_shared<MyxalNumber>(0);
    registerForGC(created);
    return created;
}

number mt::mtrue() {
    number created = std::make_shared<MyxalNumber>(1);
    registerForGC(created);
    return created;
}

string mt::mstring(std::string str) {
    string created = std::make_shared<MyxalString>(str);
    registerForGC(created);
    return created;
}

list mt::mlist(std::vector<mtype> values) {
    list created = std::make_shared<MyxalList>(values);
    registerForGC(created);
    return created;
}

list mt::mlist(std::function<mtype()> gen) {
    list created = std::make_shared<MyxalList>(gen);
    registerForGC(created);
    return created;
}