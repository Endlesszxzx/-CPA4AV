# 0 "svp_simple_003_001.c"
# 0 "<built-in>"
# 0 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4

# 1 "/usr/include/stdc-predef.h" 3 4
/* Copyright (C) 1991-2022 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <https://www.gnu.org/licenses/>.  */




/* This header is separate from features.h so that the compiler can
   include it implicitly at the start of every compilation.  It must
   not itself include <features.h> or any other header that includes
   <features.h> because the implicit include comes before any feature
   test macros that may be defined in a source file before it first
   explicitly includes a system header.  GCC knows the name of this
   header in order to preinclude it.  */

/* glibc's intent is to support the IEC 559 math functionality, real
   and complex.  If the GCC (4.9 and later) predefined macros
   specifying compiler intent are available, use them to determine
   whether the overall intent is to support these features; otherwise,
   presume an older compiler has intent to support these features and
   define these macros by default.  */
# 56 "/usr/include/stdc-predef.h" 3 4
/* wchar_t uses Unicode 10.0.0.  Version 10.0 of the Unicode Standard is
   synchronized with ISO/IEC 10646:2017, fifth edition, plus
   the following additions from Amendment 1 to the fifth edition:
   - 56 emoji characters
   - 285 hentaigana
   - 3 additional Zanabazar Square characters */
# 0 "<command-line>" 2
# 1 "svp_simple_003_001.c"

# 1 "svp_simple_003_001.c"
/*
 * racebench2.1_remarks
 * Filename:svp_simple_003_001
 * Template File:svp_simple_003
 * Created by Beijing Sunwise Information Technology Ltd. on 19/11/25.
 * Copyright © 2019年 Beijing Sunwise Information Technology Ltd. All rights reserved.
 * [说明]:
 * 主程序入口:svp_simple_003_001_main
 * 中断入口:svp_simple_003_001_isr_1,svp_simple_003_001_isr_2
 * 中断间的优先级以中断号作为标准，中断号越高，中断优先级越高。
 *
 *
 *
 *
 */

# 1 "../common.h" 1
/*
 * racebench2.0
 * Filename:common.h
 *
 * Created by Beijing Sunwise Information Technology Ltd. on 19/10/30.
 * Copyright © 2019年 Beijing Sunwise Information Technology Ltd. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 */




typedef unsigned char unsigned8;
typedef unsigned short unsigned16;
typedef unsigned int unsigned32;
typedef unsigned long long unsigned64;

typedef signed char signed8;
typedef signed short signed16;
typedef signed int signed32;
typedef signed long long signed64;

extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

void init();

extern int __VERIFIER_nondet_int();
# 18 "svp_simple_003_001.c" 2




volatile int svp_simple_003_001_global_var1;
volatile int svp_simple_003_001_global_var2;

volatile int svp_simple_003_001_global_flag = 1;
volatile int svp_simple_003_001_global_flag1 = 0;
void svp_simple_003_001_main() {
  init();
  int reader1, reader2;
  int reader3, reader4;
  idlerun();

  disable_isr(1);
  disable_isr(2);

  for (int i = 0; i < 5; i++) {
    if (i == 4) {
      reader3 = svp_simple_003_001_global_var2;
    }
  }
  for (int j = 5; j > 0; j--) {
    if (j == 4) {
      reader4 = svp_simple_003_001_global_var2;
    }
  }

  enable_isr(1);
  for (int i = 0; i < 5; i++) {
    if (i == 4) {
      reader1 = svp_simple_003_001_global_var1;
    }
  }
  for (int j = 5; j > 0; j--) {
    if (j == 4) {
      reader2 = svp_simple_003_001_global_var1;
    }
  }
}

void svp_simple_003_001_isr_1() {
  enable_isr(2);
  svp_simple_003_001_global_var2 = 999;

  if (svp_simple_003_001_global_flag == 1) {
    svp_simple_003_001_global_var1 = 999;
  } else if (svp_simple_003_001_global_flag1 == 2) {
    svp_simple_003_001_global_var1 = 888;
  }
}

void svp_simple_003_001_isr_2() {
  idlerun();
  svp_simple_003_001_global_flag = 1;
}
//bug点:
//1.svp_simple_003_001_global_var1<R#50>,<W#65>,<R#55>
//误报点:
//1.svp_simple_003_001_global_var2<R#38>,<W#62>,<R#43>
//2.svp_simple_003_001_global_var1<R#50>,<W#67>,<R#55>
