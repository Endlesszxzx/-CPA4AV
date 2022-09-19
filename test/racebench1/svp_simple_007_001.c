extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();









#define MAX_LENGTH 10
#define TRIGGER 9

volatile int svp_simple_007_001_global_var;

volatile int svp_simple_007_001_global_array[5];
void svp_simple_015_001_main() {
  init();
  int reader1, reader2;

  

  svp_simple_007_001_global_array[svp_simple_007_001_global_var] = 0x01;

  reader2 = svp_simple_007_001_global_array[svp_simple_007_001_global_var];

  int i = rand();
  if (i == 2)
    svp_simple_007_001_global_array[i] = 0x02;
  else
    svp_simple_007_001_global_array[i] = 0x09;

  reader1 = svp_simple_007_001_global_array[2];
}

void svp_simple_001_001_isr_1() {
  idlerun();
  svp_simple_007_001_global_array[2] = 0x09;

  svp_simple_007_001_global_var += 1;
  svp_simple_007_001_global_array[svp_simple_007_001_global_var] = 0x09;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_007_001_global_array<W#37>,<W#46>,<R#41>
//误报点:
//1.svp_simple_007_001_global_array<W#32>,<W#50>,<R#34>
//2.svp_simple_007_001_global_array<W#40>,<W#47>,<R#42>