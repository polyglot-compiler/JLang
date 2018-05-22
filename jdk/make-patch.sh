#!/bin/bash
if [ $# -eq 0 ]; then
    echo "Example usage: $0 java/lang/Object.java"
    exit 1
fi

SRC=src

set -e
mkdir -p `dirname patches/"$1"`
diff -u "${SRC}.orig/$1" "$SRC/$1" > "patches/${1%.java}.patch"
