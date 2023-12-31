// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include<pthread.h>
#include<assert.h>
#include "racemacros.h"

int x = 0;
// pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_673_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex);
  if (x == 0) {
    // pthread_mutex_unlock(&mutex);
  } else {
    // pthread_mutex_unlock(&mutex);
    access(x);
  }
  // return NULL;
}

int svp_simple_673_001_main(void) {
  create_threads(t);
  access(x);
  // pthread_mutex_lock(&mutex);
  assert_racefree(x); // UNKNOWN
  // pthread_mutex_unlock(&mutex);
  join_threads(t);
  return 0;
}
