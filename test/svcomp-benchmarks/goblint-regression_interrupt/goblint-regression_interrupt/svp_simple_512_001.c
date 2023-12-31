// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

extern int __VERIFIER_nondet_int();

#include<stdio.h>
// #include<pthread.h>
#include<assert.h>

int glob;
// pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t n = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_512_001_isr_1(void *arg) {
  // pthread_mutex_lock(&n);
  glob++; // RACE!
  // pthread_mutex_unlock(&n);
  // return NULL;
}

int svp_simple_512_001_main() {
  int i = __VERIFIER_nondet_int();
  // pthread_t id;

  // Create the thread
  // pthread_create(&id, NULL, t_fun, NULL);

  printf("Do the work? ");
  if (i)
    // pthread_mutex_lock(&m);
  printf("Now we do the work..n");
  if (i)
    glob++; // RACE!
  printf("Work is completed...");
  if (i)
    // pthread_mutex_unlock(&m);

  return 0;
}
