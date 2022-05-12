#ifndef TYPES_HPP_INCLUDE
#define TYPES_HPP_INCLUDE

#include <string>
#include <vector>
#include <cstddef>
#include <functional>
#include "gc.hpp"

class MyxalNumber;
class MyxalList;

class MyxalType {
    public:
        virtual bool isList();
        virtual bool isNumber();
        virtual bool isString();

        virtual std::string asString() = 0;
        virtual MyxalNumber *asNumber();
        virtual MyxalList *asList();

        bool marked = false;
};

class MyxalNumber : public MyxalType {
    public:
        MyxalNumber(long long value);
        MyxalNumber(long double value);
        MyxalNumber(MyxalNumber &other);

        long long asWhole();
        long double asDecimal();

        bool isWhole();

        bool isNumber();

        std::string asString();

        MyxalNumber *operator+(MyxalNumber &other);
        MyxalNumber *operator-(MyxalNumber &other);
        MyxalNumber *operator*(MyxalNumber &other);
        MyxalNumber *operator/(MyxalNumber &other);
        MyxalNumber *operator%(MyxalNumber &other);
    private:
        long long value;
        long double decimal;
        bool isWholeNumber;
};

class MyxalList : public MyxalType {
    public:
        MyxalList();
        MyxalList(MyxalList &other);
        MyxalList(std::vector<MyxalType *> values);
        MyxalList(std::function<MyxalType *()> generator);
        ~MyxalList();

        bool isList();

        size_t size();

        bool hasIndex(size_t index);

        MyxalType *operator[](int index);

        std::string asString();

        MyxalList *operator+(MyxalList &other);
        MyxalList *operator+(MyxalType *other);

        class iterator {
            typedef std::forward_iterator_tag iterator_category;
            typedef MyxalType value_type;
            typedef std::ptrdiff_t difference_type;
            typedef MyxalType *pointer;
            typedef MyxalType &reference;

            public:
                iterator(MyxalList *list, size_t index);
                MyxalType *operator*();
                iterator &operator++();
                bool operator!=(iterator &other);
                bool hasNext();
            private:
                MyxalList *list;
                size_t index;
        };

        iterator begin();
        iterator end();
        
        MyxalList *map(std::function<MyxalType *(MyxalType *)> func);

        MyxalType *depend = nullptr;
    private:
        MyxalList(size_t size);
        std::vector<MyxalType *> backing;
        std::function<MyxalType *()> generator;
        bool isDone;

        void fill(size_t targetSize);
};

#endif // TYPES_HPP_INCLUDE