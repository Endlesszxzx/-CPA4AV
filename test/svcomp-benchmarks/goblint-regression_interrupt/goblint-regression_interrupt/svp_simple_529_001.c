// This file is part of the SV-Benchmarks collection of verification tasks:
// https://gitlab.com/sosy-lab/benchmarking/sv-benchmarks
//
// SPDX-FileCopyrightText: 2005-2021 University of Tartu & Technische Universität München
//
// SPDX-License-Identifier: MIT

// #include <pthread.h>

int x;

void svp_simple_529_001_isr_1(void *arg) {
  x++; // RACE!
  // return NULL;
}

int svp_simple_529_001_main() {
  // pthread_t id1, id2;

  // pthread_create(&id1, NULL, t_fun, NULL);
  // pthread_create(&id2, NULL, t_fun, NULL);

  return 0;
}
