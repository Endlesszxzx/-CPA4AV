// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

#include <stdlib.h>
// #include <pthread.h>
#include <stdio.h>

int *x;
int *y;

// pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_502_001_isr_1(void *arg) {
  // pthread_mutex_lock(&m);
  *x = 3; // NORACE
  *y = 8; // RACE!
  // pthread_mutex_unlock(&m);
  // return NULL;
}

int svp_simple_502_001_main() {
  // pthread_t id;
  int *z;

  x = malloc(sizeof(int));
  y = malloc(sizeof(int));
  z = y;

  // pthread_create(&id, NULL, t_fun, NULL);

  // pthread_mutex_lock(&m);
  printf("%dn",*x); // NORACE
  // pthread_mutex_unlock(&m);
  printf("%dn",*z); // RACE!

  return 0;
}
