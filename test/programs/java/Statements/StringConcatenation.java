// This file is part of CPAchecker,
// a tool for configurable software verification:
// https://cpachecker.sosy-lab.org
//
// SPDX-FileCopyrightText: 2007-2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

public class StringConcatenation {
  public static void main(String[] args) {
    int n = 5;
    String s = "Hello World";

    s = s + 5 + " times"; 
  }
}
