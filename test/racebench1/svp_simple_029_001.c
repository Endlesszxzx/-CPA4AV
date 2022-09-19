extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);
extern int rand();

void init();

volatile int svp_simple_029_001_tm_blocks[10];
volatile int svp_simple_029_001_average_adjust_flag;
volatile int svp_simple_029_001_average_adjust_count;
volatile int (*svp_simple_029_001_ptr_GetTmData)(int);
void (*svp_simple_029_001_ptr_SetTmData)(int, int);
void (*svp_simple_029_001_ptr_SetSelfCtrlFlag)(int, int, int);

void svp_simple_029_001_TmOrgFuncMap();
void svp_simple_029_001_SetSelfCtrlFlag(int tm_para, int ctrl_flag,
                                    int flag_pos);
int svp_simple_029_001_GetTmData(int tm_name);
void svp_simple_029_001_SetTmData(int tm_name, int tm_data);

void svp_simple_029_001_init();

void svp_simple_015_001_main() {
  int svp_simple_029_001_local_status = 1;
  svp_simple_029_001_init();
  svp_simple_029_001_TmOrgFuncMap();
  
  if (svp_simple_029_001_local_status == 1) {
    svp_simple_029_001_ptr_SetSelfCtrlFlag(36, 0xFF, 0);
  }
}
void svp_simple_029_001_init() {
  for (int i = 0; i < 10; i++) {
    svp_simple_029_001_tm_blocks[i] = rand();
  }
  svp_simple_029_001_average_adjust_flag = 0xFF;

  init();
}

void svp_simple_029_001_TmOrgFuncMap() {
  svp_simple_029_001_ptr_SetTmData = svp_simple_029_001_SetTmData;
  svp_simple_029_001_ptr_GetTmData = svp_simple_029_001_GetTmData;
  svp_simple_029_001_ptr_SetSelfCtrlFlag = svp_simple_029_001_SetSelfCtrlFlag;
}
void svp_simple_029_001_SetSelfCtrlFlag(int tm_para, int ctrl_flag,
                                    int flag_pos) {
  int tmp1;
  int tmp2;
  int ctrl_sts;

  if (ctrl_flag > 0x80) {
    tmp1 = 1;
  } else {
    tmp1 = 0;
  }

  tmp1 <<= flag_pos;
  tmp2 = 1;
  tmp2 <<= ~tmp2;

  ctrl_sts = svp_simple_029_001_ptr_GetTmData(tm_para);       
  ctrl_sts -= svp_simple_029_001_ptr_GetTmData(tm_para + 1);  
  ctrl_sts |= tmp1;

  svp_simple_029_001_ptr_SetTmData(tm_para, ctrl_sts);  
}
int svp_simple_029_001_GetTmData(int tm_name) {
  return svp_simple_029_001_tm_blocks[tm_name]; 
}
void svp_simple_029_001_SetTmData(int tm_name, int tm_data) {
  svp_simple_029_001_tm_blocks[tm_name] = tm_data; 
}

void svp_simple_001_001_isr_1() {
  if (svp_simple_029_001_average_adjust_flag == 0xFF) {
    svp_simple_029_001_average_adjust_count++;
    svp_simple_029_001_ptr_SetTmData(
        36, svp_simple_029_001_average_adjust_count); 
  } else {
    svp_simple_029_001_average_adjust_count = 0;
    svp_simple_029_001_ptr_SetTmData(36, svp_simple_029_001_average_adjust_count);
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
// 1.svp_simple_029_001_tm_blocks[36] <R, #71>, <W, #74>, <W, #74>

// 误报点：
//1.svp_simple_029_001_tm_blocks[36] <R, #80>, <W, #83>, <R, #80>
