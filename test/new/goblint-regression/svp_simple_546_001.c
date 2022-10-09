
#include <stdio.h>

static int data1;
static int data2;


void svp_simple_546_001_isr_1(void *arg) {

  data1++;            // RACE!
  printf("%d",data2); // RACE!

}

int svp_simple_546_001_main(void) {

  printf("%d",data1); // RACE!
  data2++;            // RACE!

  return 0;
}

