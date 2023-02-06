extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();




void svp_simple_018_001_main();

int svp_simple_018_001_func1();

int svp_simple_018_001_func2();

void svp_simple_018_001_isr_func1();

volatile int svp_simple_018_001_para1;
volatile int svp_simple_018_001_para2;

void svp_simple_015_001_main() {
  init();
  int svp_simple_018_001_perimete = 0;
  int svp_simple_018_001_area = 0;
  svp_simple_018_001_perimete = svp_simple_018_001_func1();
  svp_simple_018_001_area = svp_simple_018_001_func2();
}

int svp_simple_018_001_func1() {
  int perimete = 0;
  perimete = 2 * svp_simple_018_001_para1 +
             svp_simple_018_001_para2;
  return perimete;
}

int svp_simple_018_001_func2() {
  int area = 0;
  area = svp_simple_018_001_para1 +
         svp_simple_018_001_para2 +
         svp_simple_018_001_para2;
  return area;
}

void svp_simple_018_001_isr_func1() {
  svp_simple_018_001_para2 = 1;
}

void svp_simple_001_001_isr_1() {
//  idlerun();
  svp_simple_018_001_para1 = 2;
}

void svp_simple_001_001_isr_2() {
//  idlerun();
  svp_simple_018_001_isr_func1();
}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_018_001_para1<R#35>,<W#54>,<R#42>
//2.svp_simple_018_001_para2<R#36>,<W#49>,<R#43>
//3.svp_simple_018_001_para2<R#43>,<W#49>,<R#44>