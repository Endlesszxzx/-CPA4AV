extern void idlerun();

extern void enable_isr(int);

extern void disable_isr(int);

extern int rand();

void init();









#define MAX_LENGTH 10
#define TRIGGER 9

volatile int svp_simple_002_001_global_array[MAX_LENGTH];
volatile int casee2_global_var;

void svp_simple_015_001_main() {
  init();
  idlerun();
}

void svp_simple_001_001_isr_1() {
  int mininum, maxnum;
  for (int i = 0; i < MAX_LENGTH; i++) {
    if (i == TRIGGER) svp_simple_002_001_global_array[TRIGGER] = 1;
    if (i == MAX_LENGTH + 1)
      svp_simple_002_001_global_array[TRIGGER] = 1;
  }
  mininum = svp_simple_002_001_global_array[TRIGGER] - 10;

  maxnum = svp_simple_002_001_global_array[0] + 10;
}

void svp_simple_001_001_isr_2() {
  idlerun();
  svp_simple_002_001_global_array[TRIGGER] = 999;

}

void init() { enable_isr(-1); }

void idlerun() {
  int i = 0;
  for (i = 0; i <= 10; i++) {
    //		print2("Running....");
  }
}



//bug点:
//1.svp_simple_001_001_global_array <W#32>,<R#55>,<W#35>
//误报点：
//1.svp_simple_001_001_global_var<W#43><R#63><W#44>
//2.svp_simple_001_001_global_array<W#32><R#60><W#35>