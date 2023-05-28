extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();









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
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_012_001_global_var<W#29>,<R#36>,<W#31>