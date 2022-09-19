# 1 "svp_simple_526_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_526_001.c"
# 10 "svp_simple_526_001.c"
int data;
int *p = &data, *q;



void svp_simple_526_001_isr_1(void *arg) {

  *p = 8;


}

int svp_simple_526_001_main() {


  q = p;

  *q = 8;

  return 0;
}
