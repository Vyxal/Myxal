#ifndef TYPES_HPP_INCLUDE
#define TYPES_HPP_INCLUDE

#include <string>
#include <vector>
#include <cstddef>
#include <functional>
#include "gc.hpp"

class MyxalNumber;
class MyxalList;
class MyxalString;

typedef std::shared_ptr<MyxalNumber> number;
typedef std::shared_ptr<MyxalList> list;
typedef std::shared_ptr<MyxalString> string;

class MyxalType : public std::enable_shared_from_this<MyxalType> {
    public:
        virtual bool isList();
        virtual bool isNumber();
        virtual bool isString();

        virtual std::string asString() = 0;

        virtual void mark(bool mark = true);
        bool marked = false;
};

class MyxalNumber : public MyxalType {
    public:
        MyxalNumber(int value);
        MyxalNumber(long long value);
        MyxalNumber(double value);
        MyxalNumber(long double value);
        MyxalNumber(bool value);
        MyxalNumber(MyxalNumber &other);

        long long asWhole();
        long double asDecimal();

        bool isWhole();

        bool isNumber();

        std::string asString();

        number add(MyxalNumber other);
        number sub(MyxalNumber other);
        number mul(MyxalNumber other);
        number div(MyxalNumber other);
        number mod(MyxalNumber other);
    private:
        long long value;
        long double decimal;
        bool isWholeNumber;
};

class MyxalList : public MyxalType {
    public:
        MyxalList();
        MyxalList(size_t size);
        MyxalList(MyxalList &other);
        MyxalList(std::vector<mtype> values);
        MyxalList(std::function<mtype()> generator);

        bool isList();

        size_t size();
        bool isEmpty();

        bool hasIndex(size_t index);

        mtype operator[](int index);

        std::string asString();

        list operator+(MyxalList &other);
        list operator+(mtype other);

        class iterator {
            typedef std::forward_iterator_tag iterator_category;
            typedef MyxalType value_type;
            typedef std::ptrdiff_t difference_type;
            typedef mtype pointer;
            typedef MyxalType &reference;

            public:
                iterator(list list, size_t index);
                mtype operator*();
                iterator &operator++();
                bool operator!=(iterator &other);
                bool hasNext();
            private:
                list list;
                size_t index;
        };

        iterator begin();
        iterator end();
        
        list map(std::function<mtype(mtype)> func);

        void mark(bool mark = true);
        void freeAllReferences();
    private:
        std::vector<mtype> backing;
        std::function<mtype()> generator;
        bool isDone;

        list depend = nullptr;

        void fill(size_t targetSize);
};

class MyxalString : public MyxalType {
    public:
        MyxalString(std::string value);
        MyxalString(MyxalString &other);

        bool isString();

        std::string asString();
    private:
        std::string value;
};

list asList(mtype item);
number asNumber(mtype item);
string asString(mtype item);

namespace mt {
    number mnumber(int num);
    number mnumber(double num);
    number mnumber(long double num);
    number mnumber(long long num);
    number mfalse();
    number mtrue();
    string mstring(std::string str);
    list mlist(std::vector<mtype> values);
    list mlist(std::function<mtype()> gen);
}

#endif // TYPES_HPP_INCLUDE