// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>

int data;
// pthread_mutex_t m[10];

void svp_simple_548_001_isr_1(void *arg) {
  // pthread_mutex_lock(&m[4]);
  data++; // NORACE
  // pthread_mutex_unlock(&m[4]);
  // return NULL;
}

int svp_simple_548_001_main() {
  for (int i = 0; i < 10; i++)
    // pthread_mutex_init(&m[i], NULL);

  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);
  // pthread_mutex_lock(&m[4]);
  data++; // NORACE
  // pthread_mutex_unlock(&m[4]);
  return 0;
}

