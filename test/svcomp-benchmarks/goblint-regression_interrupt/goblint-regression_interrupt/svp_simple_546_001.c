// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>
#include <stdio.h>

static int data1;
static int data2;
static // pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;


void svp_simple_546_001_isr_1(void *arg) {
  // pthread_rwlock_rdlock(&rwlock);
  data1++;            // RACE!
  printf("%d",data2); // RACE!
  // pthread_rwlock_unlock(&rwlock);
  // return NULL;
}

int svp_simple_546_001_main(void) {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);

  // pthread_rwlock_rdlock(&rwlock);
  printf("%d",data1); // RACE!
  data2++;            // RACE!
  // pthread_rwlock_unlock(&rwlock);

  // pthread_join (id, NULL);
  return 0;
}

