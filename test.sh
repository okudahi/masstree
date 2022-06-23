#!bin/bash

PREFIX_SIZES=(0 80 640)
#PREFIX_SIZES=(0 10 20 40 80 160 320 640)

exec_test() {
    for size in "${PREFIX_SIZES[@]}"
    do
      java -Xms1g -Xmx12g Evaluate $size
    done
}

exec_test