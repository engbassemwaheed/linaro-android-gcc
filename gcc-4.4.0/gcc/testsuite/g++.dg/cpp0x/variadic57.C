// { dg-options "-std=gnu++0x" }
template<typename T, int... Dims>
struct array { 
  int foo();
};

template<typename T>
struct array<T, 0> {
  int bar();
};

template<typename T, int... Dims>
int array<T, Dims...>::foo() { }

template<typename T>
int array<T, 0>::bar() { }
