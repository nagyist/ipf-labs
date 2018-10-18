#!/bin/sh
wget -q https://raw.githubusercontent.com/oehf/ipf-labs/master/maven/settings.xml --no-check-certificate
mvn clean deploy -DskipTests=true --settings settings.xml