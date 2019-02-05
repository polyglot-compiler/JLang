#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
BASE_DIR=$(realpath $(dirname "$DIR"))
JDK7=$(realpath "$JDK7")
JDK7_LIB="$JDK7"/jre/lib
if [ "$(uname)" = "Linux" ]
then
    JDK7_LIB="$JDK7_LIB"/amd64
fi

CLANG=clang++
LLC=llc

if ! [ -z "$CLANG_VERSION" ]
then
    CLANG=clang++-"$CLANG_VERSION"
    LLC=llc-"$CLANG_VERSION"
fi

if [ "$#" -lt 1 ]
then
    printf "Need to supply at least 1 input file\n"
    printf "Usage: compile_ll.sh file1.ll [file2.ll ...]\n"
    exit 1
fi

if [ "$#" -eq 1 ]
then
    ARGS="$@"
else
    ARGS="${@:2}"
fi

OBJ_NAME=$(echo "$1" | cut -f 1 -d '.')".o"
"$CLANG" -Wno-override-module -lgc -g -L"$BASE_DIR"/runtime/out -ljvm -L"$JDK"/out -ljdk -Wl,-rpath,"$JDK7_LIB" -Wl,-rpath,"$JDK"/out -Wl,-rpath,"$BASE_DIR"/runtime/out -rdynamic -o "$OBJ_NAME" "$ARGS"

printf "Wrote compiled binary to $OBJ_NAME\n"


