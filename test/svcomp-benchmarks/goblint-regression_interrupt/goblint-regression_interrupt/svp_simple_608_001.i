# 1 "svp_simple_608_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_608_001.c"







# 1 "/usr/include/assert.h" 1 3 4
# 35 "/usr/include/assert.h" 3 4
# 1 "/usr/include/features.h" 1 3 4
# 461 "/usr/include/features.h" 3 4
# 1 "/usr/include/x86_64-linux-gnu/sys/cdefs.h" 1 3 4
# 452 "/usr/include/x86_64-linux-gnu/sys/cdefs.h" 3 4
# 1 "/usr/include/x86_64-linux-gnu/bits/wordsize.h" 1 3 4
# 453 "/usr/include/x86_64-linux-gnu/sys/cdefs.h" 2 3 4
# 1 "/usr/include/x86_64-linux-gnu/bits/long-double.h" 1 3 4
# 454 "/usr/include/x86_64-linux-gnu/sys/cdefs.h" 2 3 4
# 462 "/usr/include/features.h" 2 3 4
# 485 "/usr/include/features.h" 3 4
# 1 "/usr/include/x86_64-linux-gnu/gnu/stubs.h" 1 3 4
# 10 "/usr/include/x86_64-linux-gnu/gnu/stubs.h" 3 4
# 1 "/usr/include/x86_64-linux-gnu/gnu/stubs-64.h" 1 3 4
# 11 "/usr/include/x86_64-linux-gnu/gnu/stubs.h" 2 3 4
# 486 "/usr/include/features.h" 2 3 4
# 36 "/usr/include/assert.h" 2 3 4
# 66 "/usr/include/assert.h" 3 4




# 69 "/usr/include/assert.h" 3 4
extern void __assert_fail (const char *__assertion, const char *__file,
      unsigned int __line, const char *__function)
     __attribute__ ((__nothrow__ , __leaf__)) __attribute__ ((__noreturn__));


extern void __assert_perror_fail (int __errnum, const char *__file,
      unsigned int __line, const char *__function)
     __attribute__ ((__nothrow__ , __leaf__)) __attribute__ ((__noreturn__));




extern void __assert (const char *__assertion, const char *__file, int __line)
     __attribute__ ((__nothrow__ , __leaf__)) __attribute__ ((__noreturn__));



# 9 "svp_simple_608_001.c" 2

# 9 "svp_simple_608_001.c"
extern void abort(void);
void reach_error() { 
# 10 "svp_simple_608_001.c" 3 4
                    ((void) sizeof ((
# 10 "svp_simple_608_001.c"
                    0
# 10 "svp_simple_608_001.c" 3 4
                    ) ? 1 : 0), __extension__ ({ if (
# 10 "svp_simple_608_001.c"
                    0
# 10 "svp_simple_608_001.c" 3 4
                    ) ; else __assert_fail (
# 10 "svp_simple_608_001.c"
                    "0"
# 10 "svp_simple_608_001.c" 3 4
                    , "svp_simple_608_001.c", 10, __extension__ __PRETTY_FUNCTION__); }))
# 10 "svp_simple_608_001.c"
                             ; }
void __VERIFIER_assert(int cond) { if(!(cond)) { ERROR: {reach_error();abort();} } }



int glob1 = 0;



void svp_simple_608_001_isr_1(void *arg) {


  glob1 = 5;




  __VERIFIER_assert(glob1 == 5);
  glob1 = 0;




}

int svp_simple_608_001_main(void) {

  __VERIFIER_assert(glob1 == 0);




  return 0;
}
