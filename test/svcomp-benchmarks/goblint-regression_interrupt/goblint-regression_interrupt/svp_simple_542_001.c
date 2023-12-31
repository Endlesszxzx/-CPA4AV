// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>
#include <stdio.h>

int myglobal;
// pthread_mutex_t mutex1 = PTHREAD_MUTEX_INITIALIZER;
// pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;

int f() {
  return 5;
}

void svp_simple_542_001_isr_1(void *arg) {
  // pthread_mutex_lock(&mutex1);
  myglobal=f(); // RACE!
  // pthread_mutex_unlock(&mutex1);
  // return NULL;
}

int svp_simple_542_001_main(void) {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);
  // pthread_mutex_lock(&mutex2);
  myglobal=myglobal+1; // RACE!
  // pthread_mutex_unlock(&mutex2);
  // pthread_join (id, NULL);
  return 0;
}
