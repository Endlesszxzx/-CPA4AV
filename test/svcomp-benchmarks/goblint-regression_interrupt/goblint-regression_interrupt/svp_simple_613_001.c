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


// #include<pthread.h>

int glob1 = 0;
// pthread_mutex_t mutex1 = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;

// The question is how to compute these S[g] sets?
// They are given in the paper. Should it be as large as possible?

void svp_simple_613_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex1);
  // pthread_mutex_lock(&mutex2);
  glob1 = 5;
  // pthread_mutex_unlock(&mutex2);
  // But if s[g] = {mutex1,mutex2}, we publish here.
  // pthread_mutex_lock(&mutex2);
  __VERIFIER_assert(glob1 == 5);
  glob1 = 0;
  // pthread_mutex_unlock(&mutex1);
  // pthread_mutex_unlock(&mutex2);
  // return NULL;
}

int svp_simple_613_001_main(void) {
  // pthread_t id;
  __VERIFIER_assert(glob1 == 0);
  // pthread_create(&id, NULL, t_fun, NULL);
  // pthread_mutex_lock(&mutex1);
  __VERIFIER_assert(glob1 == 0);
  // pthread_mutex_unlock(&mutex1);
  // pthread_join (id, NULL);
  return 0;
}
