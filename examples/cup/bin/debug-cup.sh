#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
export JAVA_HOME="$JDK7"
gdb "$DIR"/Main.o "$@"
