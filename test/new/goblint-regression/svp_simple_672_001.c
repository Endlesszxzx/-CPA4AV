
int x = 0;

void svp_simple_672_001_isr_1(void *arg) {

  if (x == 0) {
    assert_racefree(x);

  } else {

    access(x);
  }

}

int svp_simple_672_001_main(void) {
  create_threads(t);

  access(x);
  assert_racefree(x);

  join_threads(t);
  return 0;
}
