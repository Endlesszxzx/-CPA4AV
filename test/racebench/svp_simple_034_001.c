extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);
extern int __VERIFIER_nondet_int();
extern int rand();


volatile int x;
volatile int y;


int svp_simple_034_001_main() {
  y=1;
  x=3;
  int r1=x;
}

void svp_simple_001_001_isr_1() {
  if(y==2){
    x=2;
  }
  else{
    y=2;
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
// 1.svp_simple_030_001_gloable_var <R, #30>, <W, #44>, <W, #31>

// 误报点：
// 1.svp_simple_030_001_gloable_var <R, #29>, <W, #52>, <W, #30>
// 2.svp_simple_030_001_gloable_var <R, #29>, <W, #56>, <W, #30>