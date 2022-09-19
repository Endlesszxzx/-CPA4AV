# 1 "svp_simple_012_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_012_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();
# 19 "svp_simple_012_001.c"
int svp_simple_012_001_global_var;

int svp_simple_012_001_global_pointer;



void svp_simple_015_001_main() {
  init();
  int *p = &svp_simple_012_001_global_var;

  svp_simple_012_001_global_var = 0x01;

  *p = 0x02;
}

void svp_simple_001_001_isr_1() {
  int reader1;
  reader1 = svp_simple_012_001_global_var;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
