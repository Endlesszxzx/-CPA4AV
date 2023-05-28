# 1 "test.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "test.c"
# 11 "test.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();




volatile int svp_simple_002_001_global_array[10000];
volatile int casee2_global_var;

void svp_simple_015_001_main() {
  init();
  idlerun();
}

void svp_simple_002_001_isr_1() {
  int mininum, maxnum;
  for (int i = 0; i < 10000; i++) {
    if (i == 9999) svp_simple_002_001_global_array[9999] = 1;
    if (i == 10000 + 1)
      svp_simple_002_001_global_array[9999] = 1;
  }
  mininum = svp_simple_002_001_global_array[9999] - 10;

  maxnum = svp_simple_002_001_global_array[0] + 10;
}

void svp_simple_002_001_isr_2() {
  idlerun();
  svp_simple_002_001_global_array[9999] = 999;

}
