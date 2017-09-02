#!/bin/bash

# Fix perms
chmod og=- ~/deployment_id_ecdsa

# find all report files and tar them
tar czf ~/reports.tgz $( find . -C $TRAVIS_BUILD_DIR -name 'failsafe-reports' -type d )

scp -P 222 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/deployment_id_ecdsa ~/reports.tgz deployer@monge.etc.to:
if [ $? != 0 ]; then
	echo "Failed to copy reports"
fi

exit 0

