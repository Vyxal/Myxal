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

number MyxalType::asNumber() {
    number number = std::dynamic_pointer_cast<MyxalNumber>(shared_from_this());
    if (number) {
        return number;
    } else {
        throw "Not a number";
    }
}

list MyxalType::asList() {
    list list = std::dynamic_pointer_cast<MyxalList>(shared_from_this());
    if (list) {
        return list;
    } else {
        throw "Not a list";
    }
}

MyxalNumber::MyxalNumber(int value) : MyxalNumber(static_cast<long long>(value)) {
}

MyxalNumber::MyxalNumber(long long value) {
    this->value = value;
    this->isWholeNumber = true;
    this->decimal = 0;
    registerForGC(shared_from_this());
}

MyxalNumber::MyxalNumber(double value) : MyxalNumber(static_cast<long double>(value)) {
}

MyxalNumber::MyxalNumber(long double value) {
    this->value = 0;
    this->isWholeNumber = false;
    this->decimal = value;
    registerForGC(shared_from_this());
}

MyxalNumber::MyxalNumber(bool value) : MyxalNumber(value ? 1 : 0) {
}

MyxalNumber::MyxalNumber(MyxalNumber &other) {
    this->value = other.value;
    this->isWholeNumber = other.isWholeNumber;
    this->decimal = other.decimal;
    registerForGC(shared_from_this());
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

number MyxalNumber::operator+(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return std::make_shared<MyxalNumber>(this->value + other.value);
    } else {
        // The rest are gonna end up as decimals
        return std::make_shared<MyxalNumber>(this->asDecimal() + other.asDecimal());
    }
}

number MyxalNumber::operator-(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return std::make_shared<MyxalNumber>(this->value - other.value);
    } else {
        // The rest are gonna end up as decimals
        return std::make_shared<MyxalNumber>(this->asDecimal() - other.asDecimal());
    }
}

number MyxalNumber::operator*(MyxalNumber &other) {
    if (this->isWholeNumber && other.isWholeNumber) {
        return std::make_shared<MyxalNumber>(this->value * other.value);
    } else {
        // The rest are gonna end up as decimals
        return std::make_shared<MyxalNumber>(this->asDecimal() * other.asDecimal());
    }
}

number MyxalNumber::operator/(MyxalNumber &other) {
    // We want float division here
    return std::make_shared<MyxalNumber>(this->asDecimal() / other.asDecimal());
}

number MyxalNumber::operator%(MyxalNumber &other) {
    // Mod only works on integers
    return std::make_shared<MyxalNumber>(this->asWhole() % other.asWhole());
}


// List
MyxalList::MyxalList() {
    registerForGC(shared_from_this());
    this->generator = [] { return nullptr; };
    this->isDone = true;
}

MyxalList::~MyxalList() {
    for (auto &item : this->backing) {
        item.reset();
    }
    depend.reset();
}

MyxalList::MyxalList(MyxalList &other) : MyxalList(other.size()) {
    for (auto &item : other.backing) {
        this->backing.push_back(item);
    }
    this->generator = other.generator;
    this->isDone = other.isDone;
}

MyxalList::MyxalList(std::vector<type> values) {
    this->backing = backing;
    this->generator = [] { return nullptr; };
    this->isDone = true;
    registerForGC(shared_from_this());
}

MyxalList::MyxalList(std::function<type()> generator) {
    this->backing = std::vector<type>();
    this->generator = generator;
    this->isDone = false;
    registerForGC(shared_from_this());
}

MyxalList::MyxalList(size_t size) {
    this->backing.reserve(size);
    this->generator = [] { return nullptr; };
    this->isDone = true;
    registerForGC(shared_from_this());
}

size_t MyxalList::size() {
    if (isDone) {
        return backing.size();
    } else {
        type item;
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

type MyxalList::operator[](int index) {
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
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    for (auto &item : other.backing) {
        result->backing.push_back(item);
    }
    return result;
}

list MyxalList::operator+(type other) {
    list result = std::make_shared<MyxalList>(this->backing.size() + 1);
    for (auto &item : this->backing) {
        result->backing.push_back(item);
    }
    result->backing.push_back(other);
    return result;
}

list MyxalList::map(std::function<type(type)> func) {
    size_t index = 0;
    list result = std::make_shared<MyxalList>([&] () -> type {
        if (!hasIndex(index)) {
            return nullptr;
        }
        return func(backing[index++]);
    });
    result->depend = shared_from_this();
    return result;
}

void MyxalList::fill(size_t targetSize) {
    if (!isDone) {
        type item;
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

type MyxalList::iterator::operator*() {
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
    return MyxalList::iterator(asList(), 0);
}

void MyxalList::mark(bool mark) {
    marked = mark;
    for (auto &item : backing) {
        item->mark(mark);
    }
    depend->mark(mark);
}