extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();

#define MAX_LENGTH 100
#define TRIGGER 99

volatile int svp_simple_014_001_global_var1 = 0x01;
volatile int svp_simple_014_001_global_var2 = 0x02;

int svp_simple_014_001_global_flag1 = 0;
int svp_simple_014_001_global_flag2 = 1;

//void svp_simple_014_001_main() {
//  init();
//  idlerun();
//}

void svp_simple_015_001_main() {
  init();
  disable_isr(2);
  int reader1, reader2;
  int reader3, reader4;

  for (int i = 0; i < MAX_LENGTH; i++)
    if (i == TRIGGER) reader1 = svp_simple_014_001_global_var1;

  reader2 = svp_simple_014_001_global_var1;
  reader3 = svp_simple_014_001_global_var2;

  reader4 = svp_simple_014_001_global_var2;
}

void svp_simple_001_001_isr_1() {
  svp_simple_014_001_global_flag1 = 1;
  svp_simple_014_001_global_flag2 = 0;

  enable_isr(2);

  idlerun();
}

void svp_simple_001_001_isr_2() {
  if (svp_simple_014_001_global_flag1 == 1) svp_simple_014_001_global_var1 = 0x09;
  if (svp_simple_014_001_global_flag2 == 1) svp_simple_014_001_global_var2 = 0x09;
}
void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}
//bug点:
//1.svp_simple_014_001_global_var1<R#31>,<W#50>,<R#33>
//误报点:
//1.svp_simple_014_001_global_var2<R#43>,<W#59>,<R#45>