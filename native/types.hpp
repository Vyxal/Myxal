#ifndef TYPES_HPP_INCLUDE
#define TYPES_HPP_INCLUDE

#include <string>
#include <vector>
#include <cstddef>
#include <functional>
#include "gc.hpp"

class MyxalNumber;
class MyxalList;

typedef std::shared_ptr<MyxalNumber> number;
typedef std::shared_ptr<MyxalList> list;
typedef std::shared_ptr<std::string> string;

class MyxalType : public std::enable_shared_from_this<MyxalType> {
    public:
        virtual bool isList();
        virtual bool isNumber();
        virtual bool isString();

        virtual std::string asString() = 0;
        virtual number asNumber();
        virtual list asList();

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

        number operator+(MyxalNumber &other);
        number operator-(MyxalNumber &other);
        number operator*(MyxalNumber &other);
        number operator/(MyxalNumber &other);
        number operator%(MyxalNumber &other);
    private:
        long long value;
        long double decimal;
        bool isWholeNumber;
};

class MyxalList : public MyxalType {
    public:
        MyxalList();
        ~MyxalList();
        MyxalList(MyxalList &other);
        MyxalList(std::vector<type> values);
        MyxalList(std::function<type()> generator);

        bool isList();

        size_t size();
        bool isEmpty();

        bool hasIndex(size_t index);

        type operator[](int index);

        std::string asString();

        list operator+(MyxalList &other);
        list operator+(type other);

        class iterator {
            typedef std::forward_iterator_tag iterator_category;
            typedef MyxalType value_type;
            typedef std::ptrdiff_t difference_type;
            typedef type pointer;
            typedef MyxalType &reference;

            public:
                iterator(list list, size_t index);
                type operator*();
                iterator &operator++();
                bool operator!=(iterator &other);
                bool hasNext();
            private:
                list list;
                size_t index;
        };

        iterator begin();
        iterator end();
        
        list map(std::function<type(type)> func);

        void mark(bool mark = true);
    private:
        MyxalList(size_t size);
        std::vector<type> backing;
        std::function<type()> generator;
        bool isDone;

        list depend = nullptr;

        void fill(size_t targetSize);
};

#endif // TYPES_HPP_INCLUDE