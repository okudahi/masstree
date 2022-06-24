#!bin/bash

#PREFIX_SIZES=(0 80 640)
#PREFIX_SIZES=(0 10 20 40 80 160 320 640)
PREFIX_SIZES=(0 5 10 20 30 40 50 60 70 80)

exec_test() {
    for size in "${PREFIX_SIZES[@]}"
    do
      java -Xms1g -Xmx12g Evaluate $size
    done
}

exec_test