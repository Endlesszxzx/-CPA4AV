# 1 "svp_simple_018_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_018_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();




void svp_simple_018_001_main();

int svp_simple_018_001_func1();

int svp_simple_018_001_func2();

void svp_simple_018_001_isr_func1();

volatile int svp_simple_018_001_para1;
volatile int svp_simple_018_001_para2;

void svp_simple_015_001_main() {
  init();
  int svp_simple_018_001_perimete = 0;
  int svp_simple_018_001_area = 0;
  svp_simple_018_001_perimete = svp_simple_018_001_func1();
  svp_simple_018_001_area = svp_simple_018_001_func2();
}

int svp_simple_018_001_func1() {
  int perimete = 0;
  perimete = 2 * svp_simple_018_001_para1 +
             svp_simple_018_001_para2;
  return perimete;
}

int svp_simple_018_001_func2() {
  int area = 0;
  area = svp_simple_018_001_para1 +
         svp_simple_018_001_para2 +
         svp_simple_018_001_para2;
  return area;
}

void svp_simple_018_001_isr_func1() {
  svp_simple_018_001_para2 = 1;
}

void svp_simple_001_001_isr_1() {

  svp_simple_018_001_para1 = 2;
}

void svp_simple_001_001_isr_2() {

  svp_simple_018_001_isr_func1();
}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
