// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

#include <assert.h>
extern void abort(void);
void reach_error() { assert(0); }
void __VERIFIER_assert(int cond) { if(!(cond)) { ERROR: {reach_error();abort();} } }

// #include <pthread.h>

int g = 0;
// pthread_mutex_t A = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t B = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t C = PTHREAD_MUTEX_INITIALIZER;

void svp_simple_620_001_isr_1(void *arg) {
  // pthread_mutex_lock(&A);
  // pthread_mutex_lock(&C);
  // pthread_mutex_lock(&B);
  g = 5;
  // pthread_mutex_unlock(&B);
  // pthread_mutex_lock(&B);
  g = 0;
  // pthread_mutex_unlock(&B);
  // pthread_mutex_unlock(&C);
  // pthread_mutex_unlock(&A);
  // return NULL;
}

int svp_simple_620_001_main(void) {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);

  // This must be before the other to get Mine to fail for the other even with thread ID partitioning.
  // pthread_mutex_lock(&B);
  // pthread_mutex_lock(&C);
  __VERIFIER_assert(g == 0);
  // pthread_mutex_unlock(&C);
  // pthread_mutex_unlock(&B);

  // pthread_mutex_lock(&A);
  // pthread_mutex_lock(&B);
  __VERIFIER_assert(g == 0);
  // pthread_mutex_unlock(&B);
  // pthread_mutex_unlock(&A);

  // pthread_join(id, NULL);
  return 0;
}
