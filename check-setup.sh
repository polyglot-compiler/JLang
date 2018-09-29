#!/bin/bash
if [[ -z "$JDK7" ]]
then
  echo "- JDK7 environment variable must point to an installation of JDK 7"
  exit 1
fi

if [[ -r "$JDK7/bin/javac" ]]
then
  echo "+ JDK 7 found"
else
  echo "- JDK 7 compiler not found at "$(JDK7)/bin/javac
  echo "  JDK7 is not installed or the JDK7 environment variable is set incorrectly"
  exit 1
fi

if git lfs install 2>&1 >/dev/null
then
  echo "+ git lfs installed"
else
  echo "- git lfs is not installed. See https://git-lfs.github.com/"
  exit 1
fi

llvm_version="`$LLC -version | egrep LLVM.version | awk '{print $3}'`"
if [[ -z "$llvm_version" ]]
then
  echo "- llc not found. Is LLVM installed?"
fi

case "$llvm_version" in
  [56789]*) echo "+ LLVM version is up to date: $llvm_version";;
  *) echo "- LLVM version is out of date: $llvm_version"; exit 1;;
esac

clang_version="`$CLANG --version | egrep clang.version | awk '{print $3}' | awk -F'-' '{print $1}'`"

if [[ "$llvm_version" != "$clang_version" ]]
then
  echo "- llc and clang versions do not match: $llvm_version vs $clang_version"
  exit 1
fi

echo "Setup looks good"
exit 0
