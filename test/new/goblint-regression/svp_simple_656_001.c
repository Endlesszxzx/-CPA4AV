// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

extern int __VERIFIER_nondet_int();

// #include<pthread.h>
#include "racemacros.h"

void foo(int (*callback)()) {
  for (int i = 0; i < 10; i++) {
    if (__VERIFIER_nondet_int())
      callback();
  }
}


int glob;
// pthread_mutex_t mutex1 = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;


int bar() {
  // pthread_mutex_lock(&mutex1);
  access(glob);
  // pthread_mutex_unlock(&mutex1);
  return 0;
}

void svp_simple_656_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex2);
  assert_racefree(glob); // UNKNOWN
  // pthread_mutex_unlock(&mutex2);
  // return NULL;
}

int svp_simple_656_001_main() {
  create_threads(t);
  foo(bar);
  join_threads(t);
  return 0;
}
