# 1 "svp_simple_010_001.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "svp_simple_010_001.c"
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();


typedef union pack1 {
  unsigned char header;
  unsigned int data;
} svp_simple_010_001_union;

typedef struct pack2 {
  unsigned char header;
  unsigned int data;
} svp_simple_010_001_struct;

volatile svp_simple_010_001_union svp_simple_010_001_global_union;
volatile svp_simple_010_001_struct svp_simple_010_001_global_struct;

void svp_simple_015_001_main() {
  init();
  int svp_simple_010_001_local_var1 = 0x01;
  int svp_simple_010_001_local_var2 = 0x02;

  int svp_simple_010_001_local_var3 = 0x03;
  int svp_simple_010_001_local_var4 = 0x04;

  svp_simple_010_001_global_union.header = svp_simple_010_001_local_var1;
  svp_simple_010_001_global_union.data = svp_simple_010_001_local_var2;

  svp_simple_010_001_global_struct.header = svp_simple_010_001_local_var3;
  svp_simple_010_001_global_struct.data = svp_simple_010_001_local_var4;
}

void svp_simple_001_001_isr_1() {

  int reader1, reader2;

  reader1 = svp_simple_010_001_global_union.header;

  reader2 = svp_simple_010_001_global_struct.header;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {

  }
}
