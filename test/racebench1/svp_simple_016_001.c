/*
 * racebench2.1_remarks
 * Filename:svp_simple_016_001
 * Template File:svp_simple_016
 * Created by Beijing Sunwise Information Technology Ltd. on 19/11/25.
 */
extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();

#include "../common.h"

volatile int svp_simple_016_001_global_var1;

void svp_simple_016_001_main() {
  init();
  int reader1;
  svp_simple_016_001_global_var1 = 0x01;
  reader1 = svp_simple_016_001_global_var1 +
            svp_simple_016_001_global_var1 +
            svp_simple_016_001_global_var1;

}

void svp_simple_016_001_isr_1() {
  idlerun();
  svp_simple_016_001_global_var1 = 0x09;
}


void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}




//bug点:
//1.svp_simple_016_001_global_var1<W#24>,<R#33>,<R#25>
//2.svp_simple_016_001_global_var1<R#25>,<W#33,<R#26>
//3.svp_simple_016_001_global_var1<R#26>,<W#33>,<R#27>