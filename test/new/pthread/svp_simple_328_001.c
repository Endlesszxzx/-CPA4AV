
extern void __VERIFIER_atomic_begin();
extern void __VERIFIER_atomic_end();

extern void abort(void);
void reach_error() { assert(0); }

int i = 3, j = 6;

#define NUM 5
#define LIMIT (2*NUM+6)

void svp_simple_328_001_isr_1(void *arg) {
  for (int k = 0; k < NUM; k++) {
    __VERIFIER_atomic_begin();
    i = j + 1;
    __VERIFIER_atomic_end();
  }
}

void svp_simple_328_001_isr_2(void *arg) {
  for (int k = 0; k < NUM; k++) {
    __VERIFIER_atomic_begin();
    j = i + 1;
    __VERIFIER_atomic_end();
  }
}

int svp_simple_328_001_main(int argc, char **argv) {

  __VERIFIER_atomic_begin();
  int condI = i >= LIMIT;
  __VERIFIER_atomic_end();

  __VERIFIER_atomic_begin();
  int condJ = j >= LIMIT;
  __VERIFIER_atomic_end();

  if (condI || condJ) {
    ERROR: {reach_error();abort();}
  }

}
