#!/bin/bash

if [ "$TRAVIS_SECURE_ENV_VARS" != "true" ]; then
  echo "Pull request build: no access to credentials, skipping test reports publishing"
  exit 0
fi

# Fix perms
chmod og=- ~/deployment_id_ecdsa

# find all report files and tar them
tar czf ~/reports.tgz $( find . -name 'failsafe-reports' -type d )

scp -P 222 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/deployment_id_ecdsa ~/reports.tgz deployer@monge.etc.to:
if [ $? != 0 ]; then
	echo "Failed to copy reports"
fi

tar czf /tmp/website.tgz -C /tmp/website .

scp -P 222 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/deployment_id_ecdsa /tmp/website.tgz deployer@monge.etc.to:/var/www/domui

if [ $? != 0 ]; then
	echo "Failed to copy reports"
fi
ssh -p 222 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/deployment_id_ecdsa deployer@monge.etc.to "cd /var/www/domui; tar xzf website.tgz"


exit 0

