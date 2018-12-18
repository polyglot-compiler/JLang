#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
export JAVA_HOME="$JDK7"
CMD=$(realpath "$1")
"$CMD" "${@:2}"
