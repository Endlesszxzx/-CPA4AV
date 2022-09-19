# 1 "svp_simple_013_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_013_001.c"
# 19 "svp_simple_013_001.c"
volatile int svp_simple_013_001_global_var1;
volatile int svp_simple_013_001_global_var2;

int svp_simple_013_001_global_flag1 = 0;
int svp_simple_013_001_global_flag2 = 1;

void svp_simple_015_001_main() {
  init();

  disable_isr(2);



  int reader1, reader2;
  int reader3, reader4;

  for (int i = 0; i < 10; i++)
    if (i == 9) reader1 = svp_simple_013_001_global_var1;

  reader2 = svp_simple_013_001_global_var1;

  reader3 = svp_simple_013_001_global_var2;

  reader4 = svp_simple_013_001_global_var2;
}







void svp_simple_001_001_isr_1() {

  svp_simple_013_001_global_flag1 = 1;

  svp_simple_013_001_global_flag2 = 0;

  enable_isr(2);

}

void svp_simple_001_001_isr_2() {
  if (svp_simple_013_001_global_flag1 == 1) svp_simple_013_001_global_var1 = 0x01;
  if (svp_simple_013_001_global_flag2 == 1) svp_simple_013_001_global_var2 = 0x01;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
