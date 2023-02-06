extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();









volatile int svp_simple_020_001_global_var;

volatile int svp_simple_020_001_global_para;

volatile int svp_simple_020_001_global_flag = 0;

void svp_simple_020_001_main() {
  init();
  disable_isr(2);
  int reader1, reader2;
  int svp_simple_020_001_local_var1, svp_simple_020_001_local_var2;
  svp_simple_020_001_local_var1 = __VERIFER_nondet_int();
  svp_simple_020_001_local_var2 = __VERIFER_nondet_int();




  if (svp_simple_020_001_local_var1 + svp_simple_020_001_local_var2 > svp_simple_020_001_global_para)
    reader1 = svp_simple_020_001_global_var;

  if (svp_simple_020_001_local_var1 + svp_simple_020_001_local_var2 < svp_simple_020_001_global_para)
    reader2 = svp_simple_020_001_global_var;
}

void svp_simple_001_001_isr_1() {
  svp_simple_020_001_global_var = 0x01;

  svp_simple_020_001_global_flag = 1;
  enable_isr(2);
}

void svp_simple_001_001_isr_2() {
  if (svp_simple_020_001_global_flag == 1) {
    svp_simple_020_001_global_para = 11;
    svp_simple_020_001_global_var = 0x05;
  }
}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_020_001_global_var<R#37>,<W#53>,<R#40>
//2.svp_simple_020_001_global_para<R#36>,<W#52>,<R#39>
//误报点:
//1.svp_simple_020_001_global_var<R#37>,<W#44>,<R#40>

