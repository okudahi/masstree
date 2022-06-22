#!bin/bash

PREFIX_SIZES=(0 10 20 30 50 100 200 300 400 500)

exec_test() {
    for size in "${PREFIX_SIZES[@]}"
    do
      java Evaluate $size
    done
}

exec_test