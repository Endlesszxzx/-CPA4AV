// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>
#include <stdio.h>

int myglobal;

void svp_simple_597_001_isr_1(void *arg) {
  myglobal=40; // NORACE
  // return NULL;
}

int svp_simple_597_001_main(void) {
  // pthread_t id;
  // pthread_create(&id, NULL, t_fun, NULL);
  // pthread_join (id, NULL);
  return 0;
}
