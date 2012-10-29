#!/bin/bash
set -eu # better error handling

# Pipe multiple errors logs (apache and application) into a single combined error log.
# filter the combined error for important messages, send these to GTalk.

GTALK_ACCOUNT=''
GTALK_PASSWORD=''

ERROR_LOG='errors.log'
MESSAGE_LOG='messages.log'

# error logs
LOGS=('/var/log/apache2/ssl-error_log' '/var/log/apache2/error.log')

# pipe logs into a combined error log
for log in "${LOGS[@]}"
do
    if [ -f "$log" ]
    then
        echo "Tailing: $log > $ERROR_LOG"
        tail -F $log >> $ERROR_LOG &
    else
        echo "Unable to find: $log"
    fi
done

# filter only error from above combined error log
if [ -f "$ERROR_LOG" ]
then
    echo "Filtering: $ERROR_LOG > $MESSAGE_LOG"
    tail -F $ERROR_LOG | grep -v --line-buffered \
        -e 'PHP Deprecated' \
        -e 'PHP Notice' \
        -e 'error reading the headers' \
        -e 'Your request is invalid' \
        -e 'Directory index forbidden' \
        -e 'File does not exist' \
        -e 'Invalid multibyte sequence' \
        -e 'client denied by server' \
        -e 'Invalid URI in request' \
        -e 'Max items in guide reached' \
        -e 'client sent HTTP/1.1 request without hostname' \
        -e 'apc-warning' \
        >> $MESSAGE_LOG &
fi

echo "Running GTalk client"
# start the monitoring client
java -classpath xmpp/smack.jar:xmpp/smackx.jar:xmpp/ Log2XmppClient $GTALK_ACCOUNT $GTALK_PASSWORD $MESSAGE_LOG &

# kill process group
trap "kill 0" SIGINT SIGTERM EXIT
wait
