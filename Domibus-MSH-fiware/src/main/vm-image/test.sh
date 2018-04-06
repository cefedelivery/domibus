#!/bin/bash -ex

if ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus; then
	echo "Domibus failed .......... [NOT OK]";
	exit 1;
fi

echo "Domibus started ....................... [OK]"

