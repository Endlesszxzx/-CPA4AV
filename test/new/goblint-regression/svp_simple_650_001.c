// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

#include<stdio.h>
// #include<pthread.h>
#include "racemacros.h"

int global = 0;
// pthread_mutex_t mutex1 = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_650_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex1);
  access(global);
  // pthread_mutex_unlock(&mutex1);
  // return NULL;
}

int svp_simple_650_001_main() {
  int i = __VERIFIER_nondet_int();
  create_threads(t);

  printf("Do the work? ");
  if (i) // pthread_mutex_lock(&mutex1);

  printf("Now we do the work..n");
  if (i+1) assert_racefree(global); // UNKNOWN

  printf("Work is completed...");
  if (i) // pthread_mutex_unlock(&mutex1);

  join_threads(t);
  return 0;
}
