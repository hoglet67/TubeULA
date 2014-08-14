#!/bin/bash

ant clean
ant

for block in 00 10 20 01 11 21 02 12 22
do
java -jar ulamangling.jar extract ../../attempt7/zzz_${block}.png  test_${block}.png 2>&1 | tee log_${block}.log
done


