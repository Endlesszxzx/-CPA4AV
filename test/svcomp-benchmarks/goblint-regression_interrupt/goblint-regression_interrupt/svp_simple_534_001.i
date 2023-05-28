# 1 "svp_simple_534_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_534_001.c"
# 10 "svp_simple_534_001.c"
int g;
int *g1;
int *g2;


void svp_simple_534_001_isr_1(void *arg) {

  (*g1)++;


}

int svp_simple_534_001_main(void) {

  int x;
  g1 = g2 = &g;



  (*g2)++;


  return 0;
}
