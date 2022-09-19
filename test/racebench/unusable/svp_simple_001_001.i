# 1 "svp_simple_001_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_001_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();







# 1 "../common.h" 1
# 11 "../common.h"
void idlerun();

void writeVariable(volatile unsigned char* var);

void readVariable(volatile unsigned char* var);

void lock();

void unlock();
# 18 "svp_simple_001_001.c" 2




volatile int svp_simple_002_001_global_array[10000];
volatile int casee2_global_var;

void svp_simple_015_001_main() {
  init();
  idlerun();
}

void svp_simple_001_001_isr_1() {
  int mininum, maxnum;
  for (int i = 0; i < 10000; i++) {
    if (i == 9999) svp_simple_002_001_global_array[9999] = 1;
    if (i == 10000 + 1)
      svp_simple_002_001_global_array[9999] = 1;
  }
  mininum = svp_simple_002_001_global_array[9999] - 10;

  maxnum = svp_simple_002_001_global_array[0] + 10;
}

void svp_simple_001_001_isr_2() {
  idlerun();
  svp_simple_002_001_global_array[9999] = 999;

}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
