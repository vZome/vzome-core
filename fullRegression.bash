#!/bin/bash

ATTACHMENTS=/Volumes/Matsumichi/Dropbox/vZome/attachments/

for year in $( ls ${ATTACHMENTS} ); do

  echo '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%'
  echo '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%'
  echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%          ${year}           %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
  echo '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%'
  echo '%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%'

  ./gradlew \
     -PregressionHome=${ATTACHMENTS} \
     regressionYear -PregressionYear=${year} || exit $?

done
