#!/bin/sh
exec java ${JAVA_OPTS} -cp .:./lib/* cn.mgdream.webssh.WebSshApplicationKt ${@}
