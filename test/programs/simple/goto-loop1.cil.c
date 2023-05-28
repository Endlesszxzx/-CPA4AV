// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 3 "goto-loop1.c"
int main(void) 
{ int counter ;

  {
#line 5
  counter = 0;
  LOOPSTART: 
#line 8
  if (counter < 5) {
#line 9
    counter = counter + 1;
    goto LOOPSTART;
  } else {

  }
#line 13
  if (counter == 4) {
    goto ERROR;
  } else {
    goto END;
  }
  ERROR: 
  goto ERROR;
  END: 
#line 23
  return (0);
}
}
