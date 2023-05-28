# 1 "svp_simple_011_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 1 "svp_simple_011_001.c"
# 77 "svp_simple_011_001.c"
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
# 78 "svp_simple_011_001.c" 2
# 1 "f:\\softmax\\mingw\\lib\\gcc\\mingw32\\9.2.0\\include\\stdbool.h" 1 3 4
# 79 "svp_simple_011_001.c" 2
# 1 "f:\\softmax\\mingw\\include\\errno.h" 1 3
# 34 "f:\\softmax\\mingw\\include\\errno.h" 3
       
# 35 "f:\\softmax\\mingw\\include\\errno.h" 3
# 138 "f:\\softmax\\mingw\\include\\errno.h" 3

# 147 "f:\\softmax\\mingw\\include\\errno.h" 3
 int* __attribute__((__cdecl__)) __attribute__((__nothrow__)) _errno(void);




# 80 "svp_simple_011_001.c" 2
# 105 "svp_simple_011_001.c"

# 105 "svp_simple_011_001.c"
unsigned int ACPIBASE;



# 108 "svp_simple_011_001.c" 3 4
_Bool 
# 108 "svp_simple_011_001.c"
    tco_lock;
unsigned long timer_alive;
char tco_expect_close;




char tco_write_buf;


char tco1_rld;
# 151 "svp_simple_011_001.c"

# 151 "svp_simple_011_001.c" 3 4
_Bool 
# 151 "svp_simple_011_001.c"
    tco1_cnt_b0;

# 152 "svp_simple_011_001.c" 3 4
_Bool 
# 152 "svp_simple_011_001.c"
    tco1_cnt_b1;

# 153 "svp_simple_011_001.c" 3 4
_Bool 
# 153 "svp_simple_011_001.c"
    tco1_cnt_b2;

# 154 "svp_simple_011_001.c" 3 4
_Bool 
# 154 "svp_simple_011_001.c"
    tco1_cnt_b3;

# 155 "svp_simple_011_001.c" 3 4
_Bool 
# 155 "svp_simple_011_001.c"
    tco1_cnt_b4;

# 156 "svp_simple_011_001.c" 3 4
_Bool 
# 156 "svp_simple_011_001.c"
    tco1_cnt_b5;

# 157 "svp_simple_011_001.c" 3 4
_Bool 
# 157 "svp_simple_011_001.c"
    tco1_cnt_b6;

# 158 "svp_simple_011_001.c" 3 4
_Bool 
# 158 "svp_simple_011_001.c"
    tco1_cnt_b7;



int heartbeat = 30;







int nowayout = 0;






unsigned char seconds_to_ticks(int seconds)
{


 return (seconds * 10) / 6;
}
# 265 "svp_simple_011_001.c"
int tco_timer_set_heartbeat (int t)
{
 unsigned char val;
 unsigned char tmrval;

 tmrval = seconds_to_ticks(t);


 if (tmrval > 0x3f || tmrval < 0x04)
  return -
# 274 "svp_simple_011_001.c" 3
         22
# 274 "svp_simple_011_001.c"
               ;


 do { while (tco_lock) { } } while (
# 277 "svp_simple_011_001.c" 3 4
0
# 277 "svp_simple_011_001.c"
);
 val = 0;
 val &= 0xc0;
 val |= tmrval;


 val = 0;
 do { tco_lock = 
# 284 "svp_simple_011_001.c" 3 4
0
# 284 "svp_simple_011_001.c"
; } while (
# 284 "svp_simple_011_001.c" 3 4
0
# 284 "svp_simple_011_001.c"
);

 if ((val & 0x3f) != tmrval)
  return -
# 287 "svp_simple_011_001.c" 3
         22
# 287 "svp_simple_011_001.c"
               ;

 heartbeat = t;
 return 0;
}

static int tco_timer_get_timeleft (int *time_left)
{
 unsigned char val;

 do { while (tco_lock) { } } while (
# 297 "svp_simple_011_001.c" 3 4
0
# 297 "svp_simple_011_001.c"
);


 val = 0;
 val &= 0x3f;

 do { tco_lock = 
# 303 "svp_simple_011_001.c" 3 4
0
# 303 "svp_simple_011_001.c"
; } while (
# 303 "svp_simple_011_001.c" 3 4
0
# 303 "svp_simple_011_001.c"
);

 *time_left = (int)((val * 6) / 10);

 return 0;
}
# 712 "svp_simple_011_001.c"
int cnt1, cnt2, cnt3, cnt4, cnt5;
void closer1();
void closer2();
void writer1();
void writer2();

void closer1(void ) {






        tco_write_buf = 'V';
        do { if (1) { if (!nowayout) { tco_expect_close = 0; if (tco_write_buf == 'V') { tco_expect_close = 42; } } } do { do { tco1_rld = 0x01; } while (
# 726 "svp_simple_011_001.c" 3 4
       0
# 726 "svp_simple_011_001.c"
       ); } while (
# 726 "svp_simple_011_001.c" 3 4
       0
# 726 "svp_simple_011_001.c"
       ); } while (
# 726 "svp_simple_011_001.c" 3 4
       0
# 726 "svp_simple_011_001.c"
       );
        tco_expect_close = 42;

        cnt1++;


}
void closer2_isr(void) {

        tco_write_buf = 'V';
        do { if (1) { if (!nowayout) { tco_expect_close = 0; if (tco_write_buf == 'V') { tco_expect_close = 42; } } } do { do { tco1_rld = 0x01; } while (
# 736 "svp_simple_011_001.c" 3 4
       0
# 736 "svp_simple_011_001.c"
       ); } while (
# 736 "svp_simple_011_001.c" 3 4
       0
# 736 "svp_simple_011_001.c"
       ); } while (
# 736 "svp_simple_011_001.c" 3 4
       0
# 736 "svp_simple_011_001.c"
       );
        tco_expect_close = 42;
        do { if (tco_expect_close != 42) { } else { } do { timer_alive = 0; } while (
# 738 "svp_simple_011_001.c" 3 4
       0
# 738 "svp_simple_011_001.c"
       ); tco_expect_close = 0; } while (
# 738 "svp_simple_011_001.c" 3 4
       0
# 738 "svp_simple_011_001.c"
       );
        cnt2++;


}

void writer1_isr(void ) {



        do { if (0) { if (!nowayout) { tco_expect_close = 0; if (tco_write_buf == 'V') { tco_expect_close = 42; } } } do { do { tco1_rld = 0x01; } while (
# 748 "svp_simple_011_001.c" 3 4
       0
# 748 "svp_simple_011_001.c"
       ); } while (
# 748 "svp_simple_011_001.c" 3 4
       0
# 748 "svp_simple_011_001.c"
       ); } while (
# 748 "svp_simple_011_001.c" 3 4
       0
# 748 "svp_simple_011_001.c"
       );
        cnt3++;


}

void writer2_isr(void ) {

        do { if (0) { if (!nowayout) { tco_expect_close = 0; if (tco_write_buf == 'V') { tco_expect_close = 42; } } } do { do { tco1_rld = 0x01; } while (
# 756 "svp_simple_011_001.c" 3 4
       0
# 756 "svp_simple_011_001.c"
       ); } while (
# 756 "svp_simple_011_001.c" 3 4
       0
# 756 "svp_simple_011_001.c"
       ); } while (
# 756 "svp_simple_011_001.c" 3 4
       0
# 756 "svp_simple_011_001.c"
       );
        cnt4++;


}
# 907 "svp_simple_011_001.c"
int svp_simple_011_001_main(int argc, char *argv[]) {

  tco_expect_close = 0;




    closer1();
# 942 "svp_simple_011_001.c"
}
