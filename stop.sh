#!/bin/bash
set -eu
XMPP_PID=`ps aux | grep run_xmpp.sh | grep -v grep | awk '{print $2}' | head -1`
if [ "$XMPP_PID" != "" ]; then
    echo "Stopping xmpp logger"
    kill $XMPP_PID
fi
