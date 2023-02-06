extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();




volatile int svp_simple_019_001_global_condition1 = 1;
volatile int svp_simple_019_001_global_condiiton2 = 1;
volatile int svp_simple_019_001_global_condition3 = 1;

volatile int svp_simple_019_001_global_para1;
volatile int svp_simple_019_001_global_para2;
volatile int svp_simple_019_001_global_para3;

volatile int svp_simple_019_001_global_var1;
volatile int svp_simple_019_001_global_var2;

void svp_simple_015_001_main() {
  int reader1, reader2, reader3, reader4, reader5;
  init();
  svp_simple_019_001_global_para1 = __VERIFIER_nondet_int();
  svp_simple_019_001_global_para2 = __VERIFIER_nondet_int();
  svp_simple_019_001_global_para3 = __VERIFIER_nondet_int();



  if ((svp_simple_019_001_global_para1 + svp_simple_019_001_global_para3) > svp_simple_019_001_global_para2)
    reader1 = svp_simple_019_001_global_var2;

  reader2 = svp_simple_019_001_global_var2;

  if ((svp_simple_019_001_global_condition1 == 1) && (svp_simple_019_001_global_condiiton2 == 1))
    reader3 = svp_simple_019_001_global_var1;
  idlerun();
  disable_isr(1);
  if ((svp_simple_019_001_global_condition1 == 1) && (svp_simple_019_001_global_condition3 == 1))
  {
    enable_isr(1);
    reader4 = svp_simple_019_001_global_var1;
  }

  idlerun();
  disable_isr(1);
  if ((svp_simple_019_001_global_condiiton2 == 1) && (svp_simple_019_001_global_condition3 == 0))
  {
    enable_isr(1);
    reader5 = svp_simple_019_001_global_var1;
  }

}

void svp_simple_001_001_isr_1() {
  idlerun();
  if ((svp_simple_019_001_global_para1 + svp_simple_019_001_global_para3) < svp_simple_019_001_global_para2)
    svp_simple_019_001_global_var2 = 0x55;

  svp_simple_019_001_global_condition3 = 0;

  svp_simple_019_001_global_var1 = 0x01;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_019_001_global_var1<R#40>,<W#66>,<R#54>
//误报点:
//1.svp_simple_019_001_global_var2<R#40>,<W#61>,<R#42>
//2.svp_simple_019_001_global_var1<R#45>,<W#65>,<R#49>
//3.svp_simple_019_001_global_var1<R#49>,<W#65>,<R#54>
//4.svp_simple_019_001_global_condition3<R#48><W#63>,<R#53>

//svp_simple_019_001_global_condition3:(svp_simple_019_001_global_condition3,svp_simple_015_001_main, '43, 'R)
//                               (svp_simple_019_001_global_condition3,svp_simple_001_001_isr_1, '64, 'W)
//                               (svp_simple_019_001_global_condition3,svp_simple_015_001_main, '51, 'R)
//},
//svp_simple_019_001_global_var1:(svp_simple_019_001_global_var1,svp_simple_015_001_main, '46, 'R)
//                               (svp_simple_019_001_global_var1,svp_simple_001_001_isr_1, '66, 'W)
//                               (svp_simple_019_001_global_var1,svp_simple_015_001_main, '54, 'R)