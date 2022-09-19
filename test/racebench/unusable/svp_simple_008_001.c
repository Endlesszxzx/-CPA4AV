extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();






void svp_simple_008_001_func_1();

#define MAX_LENGTH 10
#define TRIGGER 9

volatile int svp_simple_008_001_global_var;

volatile int svp_simple_008_001_global_array[100];

void svp_simple_015_001_main() {
  init();
  int p = 1;
  int q = 2;

  svp_simple_008_001_global_array[p + q] = 0x09;

  svp_simple_008_001_global_array[40] = 0x01;

  svp_simple_008_001_func_1();
}

void svp_simple_008_001_func_1() {
  int reader1, reader2;
  int i = 1;
  int j = 2;
  int p = 1;
  int q = 3;
  reader1 = svp_simple_008_001_global_array[i * 20 + j * 10];

  reader2 = svp_simple_008_001_global_array[p + q];
}

void svp_simple_001_001_isr_1() {
  for (int k = 0; k < 100; k++) svp_simple_008_001_global_array[k] = 0x05;
}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}

//bug点:
//1.svp_simple_008_001_global_array<W#32>,<W#49>,<R#43>
//误报点:
//1.svp_simple_008_001_global_array<W#33>,<W#52>,<R#48>