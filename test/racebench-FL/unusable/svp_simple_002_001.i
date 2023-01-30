# 1 "svp_simple_002_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_002_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();
# 22 "svp_simple_002_001.c"
volatile int svp_simple_002_001_global_array;
volatile int casee2_global_var;

void svp_simple_015_001_main() {
  init();

}

void svp_simple_001_001_isr_1() {
  int mininum, maxnum;
  for (int i = 0; i < 10; i++) {
    if (i == 9) svp_simple_002_001_global_array = 1;
    if (i == 10 + 1)
      svp_simple_002_001_global_array = 1;
  }
  mininum = svp_simple_002_001_global_array - 10;

  maxnum = svp_simple_002_001_global_array + 10;
}

void svp_simple_001_001_isr_2() {

  svp_simple_002_001_global_array = 999;

}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
      print2("Running....");
  }
}
