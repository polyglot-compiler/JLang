#/bin/bash
> check
EXIT_CODE=0
for i in $(ls *.sol | awk -F'.' '{print $1}')
do
    echo "$i.sol         $i.output" >> check
    diff -y -a $i.sol $i.output >> check
    FAIL=$?
    if [ $FAIL -lt 2 ]
    then
	EXIT_CODE=$(($EXIT_CODE + $FAIL))
    else
	EXIT_CODE=$(($EXIT_CODE + 1))
    fi
    if [ $FAIL -ne 0 ]
    then
	echo "$i failed"
    fi
done

if [ "$EXIT_CODE" -eq 0 ]
then
    echo "All tests Passed!"
else
    echo "$EXIT_CODE tests FAILED! :("
fi

exit $EXIT_CODE

