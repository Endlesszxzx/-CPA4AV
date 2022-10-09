extern int __VERIFIER_nondet_int();

void foo(int (*callback)()) {
  for (int i = 0; i < 10; i++) {
    if (__VERIFIER_nondet_int())
      callback();
  }
}


int glob;


void svp_simple_523_001_isr_1(void *arg) {

  glob=glob+1; // RACE!

}

int bar() {

  glob=glob+1; // RACE!

  return 0;
}

int svp_simple_523_001_main() {
  foo(bar);
  return 0;
}
