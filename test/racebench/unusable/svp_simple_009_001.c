extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();










volatile int svp_simple_009_001_p;
volatile int svp_simple_009_001_q;

volatile int svp_simple_009_001_m;

void svp_simple_015_001_main() {
  init();
  int svp_simple_009_001_local_var1 = 0x01;
  int svp_simple_009_001_local_var2 = 0x09;

  svp_simple_009_001_p = svp_simple_009_001_local_var1;
  svp_simple_009_001_q = svp_simple_009_001_local_var1;

  *svp_simple_009_001_p = 0x02;
  *svp_simple_009_001_q = 0x03;

  svp_simple_009_001_m = svp_simple_009_001_local_var2;

  *svp_simple_009_001_m = 0x06;
  *svp_simple_009_001_m = 0x06;
}

void svp_simple_001_001_isr_1() {
  int reader1, reader2;

  reader1 = svp_simple_009_001_p;
  int svp_simple_009_001_local_var3 = 0x08;
  svp_simple_009_001_m = svp_simple_009_001_local_var3;
  reader2 = *svp_simple_009_001_m;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.*svp_simple_009_001_p<W#33>,<R#45>,<W#34>
//误报点:
//1.*svp_simple_009_001_m<W#37>,<R#47>,<W#38>