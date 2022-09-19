# 1 "svp_simple_011_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_011_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();
# 19 "svp_simple_011_001.c"
int svp_simple_011_001_global_var1;
int svp_simple_011_001_global_var2;
int svp_simple_011_001_global_var3;

volatile int *svp_simple_011_001_u;

void svp_simple_015_001_main() {
  init();
  int *p = &svp_simple_011_001_global_var1;
  int *q = &svp_simple_011_001_global_var1;

  *p = 0x01;
  *q = 0x02;

  svp_simple_011_001_u = &svp_simple_011_001_global_var2;
  *svp_simple_011_001_u = 0x01;
  svp_simple_011_001_u = &svp_simple_011_001_global_var3;
  *svp_simple_011_001_u = 0x02;
}

void svp_simple_001_001_isr_1() {
  int reader1, reader2;
  int *m = &svp_simple_011_001_global_var1;
  reader1 = *m;
  reader2 = *svp_simple_011_001_u;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
