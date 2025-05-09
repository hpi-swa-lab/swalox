#!/bin/sh

LOX=$1

benchmark () {
  $LOX examples/benchmarks/harness.lox $1 20 $2 2>&1 | tee results/$LOX.$bench.out
}

benchmark sieve 1500
benchmark towers 600
benchmark list 1500
benchmark permute 1500
benchmark queens 3000
benchmark methodcall 100000
benchmark methodcall10 100000