// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

#include <stdlib.h>
// #include <pthread.h>

int glob;
// pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_500_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex);
  glob++; // RACE!
  // pthread_mutex_unlock(&mutex);
  // return NULL;
}

int svp_simple_500_001_main(void) {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);
  glob++; // RACE!
  return 0;
}
