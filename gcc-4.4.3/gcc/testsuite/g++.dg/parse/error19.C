// { dg-options "-fshow-column -fmessage-length=0   -ansi -pedantic-errors -Wno-long-long " }
// PR C++/17867

struct A
{  // { dg-message "1:candidates are:" }
  A(int);
};

const A& foo();

void bar()
{
  foo()=A(0); // { dg-error "12:no match for 'operator='" }
}
