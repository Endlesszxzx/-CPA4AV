extern void idlerun();

extern void enable_isr(int);
extern void disable_isr(int);
extern int rand();
void svp_simple_031_001_init();
int svp_simple_031_001_func_1(int a, int b);
int svp_simple_031_001_func_2(int x);
void svp_simple_031_001_func_3();

#define svp_simple_031_001_CNT (volatile unsigned int *)0x10000000
volatile int svp_simple_031_001_tc_block_rcvd_bytes_ch1;
volatile int svp_simple_031_001_tc_block_rcvd_bytes_ch2;
volatile int svp_simple_031_001_tc_block_rcvd_bytes;
volatile int svp_simple_031_001_tc_chan1_buff[256];
volatile int svp_simple_031_001_tc_chan2_buff[256];
volatile int *svp_simple_031_001_tc_buff_p;
volatile int svp_simple_031_001_gloable_var1;

struct svp_simple_031_001_tc_block_data {
  int type;
  int id;
  int valid_len;
};
void svp_simple_015_001_main() {
  svp_simple_031_001_init();
  struct svp_simple_031_001_tc_block_data tc_block;

  int ichan = 0;
  if (ichan == 0) {
    svp_simple_031_001_tc_buff_p = svp_simple_031_001_tc_chan1_buff;
    svp_simple_031_001_tc_block_rcvd_bytes =
        svp_simple_031_001_tc_block_rcvd_bytes_ch1;  
  }

  tc_block.type = *(svp_simple_031_001_tc_buff_p + 2);
  tc_block.id = *(svp_simple_031_001_tc_buff_p + 4);
  tc_block.valid_len = *(svp_simple_031_001_tc_buff_p + 5);
  svp_simple_031_001_func_3();
}

int svp_simple_031_001_func_1(int a, int b) {
  int c;
  if (a < b) {
    c = a * b;
  } else {
    c = svp_simple_031_001_func_2(10);
  }
  return c;
}
int svp_simple_031_001_func_2(int x) {
  if (svp_simple_031_001_tc_block_rcvd_bytes_ch1 > 0) {  
    return x;
  } else {
    return 0;
  }
}

void svp_simple_031_001_init() {
  svp_simple_031_001_tc_block_rcvd_bytes_ch1 = __VERIFER_nondet_int();
  svp_simple_031_001_tc_block_rcvd_bytes_ch2 = __VERIFER_nondet_int();

  init();
}

void svp_simple_001_001_isr_1() {
  unsigned svp_simple_031_001_flag_chan1;
  svp_simple_031_001_flag_chan1 = 0;

  if (svp_simple_031_001_tc_block_rcvd_bytes_ch1 >= 16) {  
    svp_simple_031_001_func_1(svp_simple_031_001_gloable_var1,
                          svp_simple_031_001_tc_block_rcvd_bytes_ch1);  
  }
}

void svp_simple_001_001_isr_2() {
  svp_simple_031_001_tc_block_rcvd_bytes_ch1 =
      (*(svp_simple_031_001_CNT) >> 3) & 0x1FFF;  
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
// bug点：
// 1.svp_simple_031_001_tc_block_rcvd_bytes_ch1 <#33,R> <#77,W>,<#70,R>
// 2.svp_simple_031_001_tc_block_rcvd_bytes_ch1 <#70,R> <#77,W>,<#72,R>
// 3.svp_simple_031_001_tc_block_rcvd_bytes_ch1 <#72,R> <#77,W>,<#52,R>