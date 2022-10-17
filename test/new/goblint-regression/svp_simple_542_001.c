
int myglobal;

int f() {
  return 5;
}
void svp_simple_542_001_isr_1(void *arg) {
    myglobal=f();
    if(myglobal == 5){
          int tmp = 1;
    }
}

int svp_simple_542_001_main(void) {
  int tmp=myglobal+1;
  myglobal = tmp;

  myglobal = f();
  myglobal = f();
  return 0;
}