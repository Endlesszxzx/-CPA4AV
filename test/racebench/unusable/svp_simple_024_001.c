extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();




union bytetoword {
  unsigned char bytedata[2];
  unsigned int worddata;
};

volatile int svp_simple_024_001_global_var;
volatile int *svp_simple_024_001_global_array[100];
volatile int svp_simple_024_001_reset_RT;
unsigned int svp_simple_024_001_RTData(int *array, int size);
void svp_simple_024_001_init();

void svp_simple_015_001_main() {
  union bytetoword svp_simple_024_001_local_a, svp_simple_024_001_local_b;
  svp_simple_024_001_init();
  svp_simple_024_001_local_a.worddata = svp_simple_024_001_RTData(svp_simple_024_001_global_array, 0);  
  svp_simple_024_001_local_b.worddata = svp_simple_024_001_RTData(svp_simple_024_001_global_array, 1);  
  int svp_simple_024_001_local_casereg =
      svp_simple_024_001_local_a.worddata | svp_simple_024_001_local_b.worddata & 0x02f0;

  switch (svp_simple_024_001_local_casereg) {
    case 0x02f0:
      svp_simple_024_001_reset_RT = 1;
      break;
    case 0x0390:
      svp_simple_024_001_reset_RT = 0;
      break;
    default:
      break;
  }
}
void svp_simple_024_001_init() {
  svp_simple_024_001_global_var = rand();

  init();
}

unsigned int svp_simple_024_001_RTData(int *array, int index) {
  union bytetoword svp_simple_024_001_local_r;
  svp_simple_024_001_local_r.bytedata[0] = array[index + 1];  
  svp_simple_024_001_local_r.bytedata[1] = array[index];      

  return svp_simple_024_001_local_r.worddata;
}
void svp_simple_001_001_isr_1() {
  for (int i = 0; i < 100; i++) {
    svp_simple_024_001_global_array[i] = 0; 
  }
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
// bug点：
// 1.svp_simple_024_001_global_array[1] <R, #52>, <W, #59>, <R, #53>