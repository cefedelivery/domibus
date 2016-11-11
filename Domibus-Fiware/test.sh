#!/bin/bash -ex
if ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus/home; then 
	echo "Test failed .......... [NOT OK]"; 
	exit 1;
fi

echo "Test passed ....................... [OK]"

