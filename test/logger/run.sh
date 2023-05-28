#!/bin/bash

# rm -rf ./pthread-ext_interrupt/*
# ./substitute-pthread-ext.sh ../pthread-ext 700

# echo "cd dir pthread-ext_interrupt"

# cd pthread-ext_interrupt

ls | grep -E ".*\.c" | while read line
do
	gcc -E -o "${line%.c}.i" "$line"
	echo "preprocess $line ==> ${line%.c}.i done!"
done
