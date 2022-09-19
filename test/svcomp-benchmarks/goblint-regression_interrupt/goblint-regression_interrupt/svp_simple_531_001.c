// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>
#include <stdio.h>

int global;
// pthread_mutex_t gm = PTHREAD_MUTEX_INITIALIZER;

void bad() {
  global++; // NORACE
}
void good() {
  // pthread_mutex_lock(&gm);
  global++; // NORACE
  // pthread_mutex_unlock(&gm);
}

void (*f)() = good;
// pthread_mutex_t fm = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_531_001_isr_1(void *arg) {
  void (*g)();

  // pthread_mutex_lock(&fm);
  g = f; // NORACE
  // pthread_mutex_unlock(&fm);

  g();
  // return NULL;
}

int svp_simple_531_001_main() {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);

  // pthread_mutex_lock(&fm);
  f = good; // NORACE
  // pthread_mutex_unlock(&fm);

  // pthread_mutex_lock(&gm);
  printf("global: %dn", global); // NORACE
  // pthread_mutex_unlock(&gm);

  return 0;
}
