#!/bin/bash

echo "批量输出"

while read line

do

    echo "Begin to execute:"

    echo $line

    $line

    echo "End"

done < test.txt