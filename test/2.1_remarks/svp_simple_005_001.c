/*
 * racebench2.1_remarks
 * Filename:svp_simple_005_001
 * Template File:svp_simple_005
 * Created by Beijing Sunwise Information Technology Ltd. on 19/11/25.
 * Copyright © 2019年 Beijing Sunwise Information Technology Ltd. All rights reserved.
 * [说明]:
 * 主程序入口:svp_simple_005_001_main
 * 中断入口:svp_simple_005_001_isr_1
 * 中断间的优先级以中断号作为标准，中断号越高，中断优先级越高。
 *
 *
 *
 *
 */

#include "../common.h"

#define MAX_LENGTH 5
#define TRIGGER 4
#define TRIGGER1 3

volatile int svp_simple_005_001_global_condition = 0;

volatile int svp_simple_005_001_global_var;

void svp_simple_005_001_main() {
  init();
  for (int i = 0; i < MAX_LENGTH; i++) {
    for (int j = 0; j < MAX_LENGTH; j++) {
      if ((i == TRIGGER) && (j == TRIGGER1))
        svp_simple_005_001_global_var = 0x01;
    }
  }

  if (svp_simple_005_001_global_condition ==
      1)
    svp_simple_005_001_global_var = 0x09;

  svp_simple_005_001_global_var = 0x05;
}

void svp_simple_005_001_isr_1() {
  idlerun();
  int reader;
  reader = svp_simple_005_001_global_var;
}
//bug点:
//1.svp_simple_005_001_global_var<W#32>,<R#46>,<W#40>
//误报点:
//1.svp_simple_005_001_global_var<W#32>,<R#46>,<W#38>
//2.svp_simple_005_001_global_var<W#38>,<R#46>,<W#40>