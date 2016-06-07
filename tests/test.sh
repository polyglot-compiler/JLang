#!/bin/sh

pth=`dirname $0`/../../../bin/pth

if [ ! -f "$pth" ]; then
  # TODO: make sure polyglot/bin (which is where pth lives) is in your path
  pth=pth
fi

$pth -classpath ../compiler/classes pthScript
