#!/bin/bash
# Make a sourceforge release for the current version of the internal mumble libraries.
# This checks out the current sourceforge version of mumble-alg into /tmp/mumble-alg,
# then it checks out the mumble version into /tmp/mumblealg-orig. The mumblealg-orig
# version is stripped from it's CVS data and copied over the mumble-alg sourceforge
# version; this version is then commited.

ORIG=/tmp/mumblealg-orig
SF=/tmp/mumble-alg
LOG=/tmp/update.log
CVSSF=:ext:fjalvingh@cvs.sf.net:/cvsroot/nema

cd /tmp
rm -rf mumblealg mumblealg-orig
export CVS_RSH=ssh
cvs -d :ext:jal@cvs.mumble.to:/cvs co -P mumblealg	> $LOG
if [ $? != 0 ]
then
	echo "CVS checkout of ORIGINAL (mumble) failed."
	exit 10
fi
mv mumblealg mumblealg-orig

# Check out sourceforge version
rm -rf mumble-alg
cvs -d :ext:fjalvingh@cvs.sf.net:/cvsroot/nema co -P mumble-alg >> $LOG
if [ $? != 0 ]
then
	echo "CVS checkout of SOURCEFORGE VERSION failed."
	exit 10
fi

echo "Checkouts completed. Cleaning out source copy."
find $ORIG -name 'CVS' -type d -print -exec rm -rf {} \;
find $ORIG -name '*~' -exec rm {} \;
rm -rf $ORIG/bin
rm -rf $ORIG/classes

echo "Crud removed. Copy new original over source"
cp -R $ORIG/* $SF

echo "Adding all new files/directories (and adding old ones too by the by)"
cd $SF
find . | grep -vr "\(.*/CVS/.*\)\|\(.*/CVS$\)" | xargs -n 1 cvs add

