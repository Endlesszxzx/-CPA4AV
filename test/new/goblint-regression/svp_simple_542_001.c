
int myglobal;

int f() {
  return 5;
}
void svp_simple_542_001_isr_1(void *arg) {
myglobal=f();
}

int svp_simple_542_001_main(void) {
  int tmp=myglobal+1;
  myglobal = tmp;
  return 0;
}