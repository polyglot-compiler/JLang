#/bin/bash

EXP="$1"

isExpectedFailure() {
    if grep -Fxq "$1" "$EXP"
    then
	echo "0"
    else
	echo "1"
    fi
}

> check
EXIT_CODE=0
NEW_PASS=0
TESTS=0
PASSED=0
STILL_FAILING=""
for i in $(ls *.sol | awk -F'.' '{print $1}')
do
    echo "$i.sol         $i.output" >> check
    diff -y -a $i.sol $i.output >> check
    FAIL=$?
    TESTS=$(($TESTS + 1))
    NOT_EXPECTED=$(isExpectedFailure "$i")
    if [ $FAIL -ne 0 ]
    then
	if [ $NOT_EXPECTED -eq 1 ]
	then
	    EXIT_CODE=$(($EXIT_CODE + 1))
	    echo "$i failed and is a regression"
	else
	    STILL_FAILING="$STILL_FAILING""$i""\n"
	fi
    else
	PASSED=$(($PASSED +1))
	if [ $NOT_EXPECTED -eq 0 ]
	then
	    echo "$i is now passing! Update the '$EXP' file to remove this test before committing!"
	    NEW_PASS=$(($NEW_PASS + 1))
	fi
    fi
done

RED='\033[0;31m'
BLUE='\033[0;34m'
ORANGE='\033[0;33m'
GREEN='\033[0;32m'
NC='\033[0m'
echo "=============="
if [ "$EXIT_CODE" -eq 0 ]
then
    printf "${GREEN}No regressions found!${NC}\n"
else
    printf "${RED}$EXIT_CODE new tests FAILED! :(${NC}\n"
fi
echo "=============="
if [ "$NEW_PASS" -gt 0 ]
then
    printf "${BLUE}$NEW_PASS new tests PASSING!! :)${NC}\n"
fi
echo "=============="
if [ "$PASSED" -eq "$TESTS" ]
then
    printf "${GREEN}ALL tests Passing!${NC}\n"
fi
echo "=============="
if [ -z "$STILL_FAILING" ]
then
    printf ""
else
    printf "${ORANGE}The following expected failures occurred:\n""$STILL_FAILING${NC}"
fi
exit $EXIT_CODE

