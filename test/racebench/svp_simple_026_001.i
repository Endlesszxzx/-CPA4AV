# 1 "svp_simple_026_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_026_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();
# 22 "svp_simple_026_001.c"
void svp_simple_026_001_init();

volatile int svp_simple_026_001_gloable_var;
int svp_simple_026_001_main() {
  svp_simple_026_001_init();

  disable_isr(1);
  if (svp_simple_026_001_gloable_var > 12) {
    svp_simple_026_001_gloable_var = 0;
  }
  enable_isr(1);

  return 0;
}
void svp_simple_026_001_init() {
  svp_simple_026_001_gloable_var = rand();

  init();
}

void svp_simple_001_001_isr_1() {
  svp_simple_026_001_gloable_var++;
}
void svp_simple_001_001_isr_2() {
  svp_simple_026_001_gloable_var--;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
