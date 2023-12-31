// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

#include <stdlib.h>
// #include <pthread.h>
#include <stdio.h>

typedef struct {
  int x;
  int y;
} data;

data *d;

// pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_503_001_isr_1(void *arg) {
  // pthread_mutex_lock(&m);
  d->x = 3; // NORACE
  d->y = 8; // RACE!
  // pthread_mutex_unlock(&m);
  // return NULL;
}

int svp_simple_503_001_main() {
  // pthread_t id;
  data *z;

  d = malloc(sizeof(data));
  z = d;

  // pthread_create(&id, NULL, t_fun, NULL);

  // pthread_mutex_lock(&m);
  printf("%dn",d->x); // NORACE
  // pthread_mutex_unlock(&m);
  printf("%dn",z->y); // RACE!

  return 0;
}
