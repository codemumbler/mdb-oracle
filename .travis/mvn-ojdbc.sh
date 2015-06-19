#!/bin/sh -e

mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.1 -Dpackaging=jar -Dfile=$ORACLE_HOME/jdbc/lib/ojcmddbc6.jar -DgeneratePom=true