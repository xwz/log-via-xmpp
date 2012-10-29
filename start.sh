#!/bin/bash
set -eu
XMPP_PID=`ps aux | grep run_xmpp.sh | grep -v grep | awk '{print $2}' | head -1`
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd $SCRIPT_DIR
LOG_FILENAME="$SCRIPT_DIR/run_xmpp.log"
if [ "$XMPP_PID" == "" ]; then
    echo "Starting xmpp logger"
    $SCRIPT_DIR/run_xmpp.sh >> $LOG_FILENAME  2>&1 &
fi
