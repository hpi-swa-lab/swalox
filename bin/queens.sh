#!/bin/sh


for i in byopl*; do
    ./$i examples/benchmarks/harness.lox queens 10 3000 2>&1 | tee results/$i.queens.out
done