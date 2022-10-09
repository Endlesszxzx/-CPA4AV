
int myglobal;

int isr_f() {
  return 5;
}

void svp_simple_542_001_isr_1(void *arg) {
  int tmp = isr_fun();
  myglobal=tmp; // RACE!

}

int svp_simple_542_001_main(void) {
  int tmp=myglobal+1;
	myglobal = tmp;
  // myglobal=myglobal+1; // RACE!

  return 0;
}
