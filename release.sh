#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing S-RAMP UI (Community)"
echo "######################################"
echo ""
mvn -e --batch-mode -DignoreSnapshots=true clean release:prepare release:perform
