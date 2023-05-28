# 1 "svp_simple_021_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 1 "svp_simple_021_001.c"
# 80 "svp_simple_021_001.c"
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
# 81 "svp_simple_021_001.c" 2

# 1 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stdbool.h" 1 3 4
# 83 "svp_simple_021_001.c" 2
# 1 "f:\\softmax\\mingw\\include\\errno.h" 1 3
# 34 "f:\\softmax\\mingw\\include\\errno.h" 3
       
# 35 "f:\\softmax\\mingw\\include\\errno.h" 3
# 138 "f:\\softmax\\mingw\\include\\errno.h" 3

# 147 "f:\\softmax\\mingw\\include\\errno.h" 3
 int* __attribute__((__cdecl__)) __attribute__((__nothrow__)) _errno(void);




# 84 "svp_simple_021_001.c" 2
# 1 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stdint.h" 1 3 4
# 9 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stdint.h" 3 4
# 1 "f:\\softmax\\mingw\\include\\stdint.h" 1 3 4
# 34 "f:\\softmax\\mingw\\include\\stdint.h" 3 4
       
# 35 "f:\\softmax\\mingw\\include\\stdint.h" 3
# 54 "f:\\softmax\\mingw\\include\\stdint.h" 3
# 1 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stddef.h" 1 3 4
# 321 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stddef.h" 3 4
typedef short unsigned int wchar_t;
# 350 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stddef.h" 3 4
typedef short unsigned int wint_t;
# 55 "f:\\softmax\\mingw\\include\\stdint.h" 2 3



typedef signed char int8_t;
typedef unsigned char uint8_t;
typedef short int16_t;
typedef unsigned short uint16_t;
typedef int int32_t;
typedef unsigned uint32_t;
typedef long long int64_t;
typedef unsigned long long uint64_t;



typedef signed char int_least8_t;
typedef unsigned char uint_least8_t;
typedef short int_least16_t;
typedef unsigned short uint_least16_t;
typedef int int_least32_t;
typedef unsigned uint_least32_t;
typedef long long int_least64_t;
typedef unsigned long long uint_least64_t;





typedef signed char int_fast8_t;
typedef unsigned char uint_fast8_t;
typedef short int_fast16_t;
typedef unsigned short uint_fast16_t;
typedef int int_fast32_t;
typedef unsigned int uint_fast32_t;
typedef long long int_fast64_t;
typedef unsigned long long uint_fast64_t;
# 106 "f:\\softmax\\mingw\\include\\stdint.h" 3
 typedef int __intptr_t;

typedef __intptr_t intptr_t;
# 118 "f:\\softmax\\mingw\\include\\stdint.h" 3
 typedef unsigned int __uintptr_t;

typedef __uintptr_t uintptr_t;







typedef long long intmax_t;
typedef unsigned long long uintmax_t;
# 10 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stdint.h" 2 3 4
# 85 "svp_simple_021_001.c" 2
# 117 "svp_simple_021_001.c"

# 117 "svp_simple_021_001.c"
char wdtpci_write_buf;


char wdt_dc_port;


long jiffies;
# 185 "svp_simple_021_001.c"
int dev_count;


unsigned open_sem;


# 190 "svp_simple_021_001.c" 3 4
_Bool 
# 190 "svp_simple_021_001.c"
    wdtpci_lock;
int expect_close;

int io;
int irq;




int heartbeat = 60;
int wd_heartbeat;




int nowayout = 0;
# 943 "svp_simple_021_001.c"
int cnt1, cnt2, cnt3, cnt4, cnt5;
int count;
int buf;
void writer1();
void writer2();

void closer1_isr(void ) {


        wdtpci_write_buf = 'V';
        expect_close = 42;
        count = 1;

        if (count) {
            if (!nowayout) {




                if (wdtpci_write_buf != 'V') {
                    expect_close = 0;
                }
            }
        }


        if (expect_close != 42) {

        } else {


        }
        expect_close = 0;

        cnt1++;


}

void closer2_isr(void ) {


        wdtpci_write_buf = 'V';
        expect_close = 42;
        count = 1;

        if (count) {
            if (!nowayout) {




                if (wdtpci_write_buf != 'V') {
                    expect_close = 0;
                }
            }
        }


        if (expect_close != 42) {

        } else {


        }
        expect_close = 0;

        cnt2++;


}

void writer1(void ) {






        count = 0;
        expect_close = 0;

        if (count) {
            if (!nowayout) {




                if (wdtpci_write_buf != 'V') {
                    expect_close = 0;
                }
            }
        }
        cnt3++;


}

void writer2(void ) {


        count = 0;
        expect_close = 0;

        if (count) {
            if (!nowayout) {




                if (wdtpci_write_buf != 'V') {
                    expect_close = 0;
                }
            }
        }
        cnt4++;


}



int svp_simple_021_001_main(int argc, char *argv[]) {





    writer1();

    writer2();
# 1113 "svp_simple_021_001.c"
}
