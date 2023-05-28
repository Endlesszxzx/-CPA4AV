# 1 "svp_simple_019_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 1 "svp_simple_019_001.c"




# 1 "f:\\softmax\\mingw\\include\\assert.h" 1 3
# 18 "f:\\softmax\\mingw\\include\\assert.h" 3
# 1 "f:\\softmax\\mingw\\include\\_mingw.h" 1 3
# 55 "f:\\softmax\\mingw\\include\\_mingw.h" 3
       
# 56 "f:\\softmax\\mingw\\include\\_mingw.h" 3
# 66 "f:\\softmax\\mingw\\include\\_mingw.h" 3
# 1 "f:\\softmax\\mingw\\include\\msvcrtver.h" 1 3
# 35 "f:\\softmax\\mingw\\include\\msvcrtver.h" 3
       
# 36 "f:\\softmax\\mingw\\include\\msvcrtver.h" 3
# 67 "f:\\softmax\\mingw\\include\\_mingw.h" 2 3






# 1 "f:\\softmax\\mingw\\include\\w32api.h" 1 3
# 35 "f:\\softmax\\mingw\\include\\w32api.h" 3
       
# 36 "f:\\softmax\\mingw\\include\\w32api.h" 3
# 59 "f:\\softmax\\mingw\\include\\w32api.h" 3
# 1 "f:\\softmax\\mingw\\include\\sdkddkver.h" 1 3
# 35 "f:\\softmax\\mingw\\include\\sdkddkver.h" 3
       
# 36 "f:\\softmax\\mingw\\include\\sdkddkver.h" 3
# 60 "f:\\softmax\\mingw\\include\\w32api.h" 2 3
# 74 "f:\\softmax\\mingw\\include\\_mingw.h" 2 3
# 174 "f:\\softmax\\mingw\\include\\_mingw.h" 3
# 1 "f:\\softmax\\mingw\\include\\features.h" 1 3
# 39 "f:\\softmax\\mingw\\include\\features.h" 3
       
# 40 "f:\\softmax\\mingw\\include\\features.h" 3
# 175 "f:\\softmax\\mingw\\include\\_mingw.h" 2 3
# 19 "f:\\softmax\\mingw\\include\\assert.h" 2 3
# 38 "f:\\softmax\\mingw\\include\\assert.h" 3
 
# 38 "f:\\softmax\\mingw\\include\\assert.h" 3
       void __attribute__((__cdecl__)) __attribute__((__nothrow__)) _assert (const char*, const char*, int) __attribute__((__noreturn__));
# 6 "svp_simple_019_001.c" 2


# 7 "svp_simple_019_001.c"
int a=0;
int b=0;
int c=0;
int x=0;
void f1();
void f2();

const int __CPROVER_thread_priorities[] = {5, 1};
const char* __CPROVER_threads[] = {"c::f1",
                                   "c::f2"};


void f1_isr(void)
{

        a = 1;



        if (a !=1) {

        }
        a = 0;


}


void f2_isr(void)
{



        a = 1;



        if (a !=1) {

        }
        a = 0;


}


int svp_simple_019_001_main()
{
# 64 "svp_simple_019_001.c"
}
