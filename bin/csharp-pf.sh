#!/bin/sh

SCRIPT="$0"
SCALA_RUNNER_VERSION="2.10"

while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

if [ ! -d "${APP_DIR}" ]; then
  APP_DIR=`dirname "$SCRIPT"`/..
  APP_DIR=`cd "${APP_DIR}"; pwd`
fi

cd $APP_DIR

# if you've executed sbt assembly previously it will use that instead.
export JAVA_OPTS="${JAVA_OPTS} -XX:MaxPermSize=256M -Xmx1024M -DloggerPath=conf/log4j.properties"
ags="$@ samples/client/pf/csharp/CSharpPfCodegen.scala https://pf1.pinggcs.com:9999/pf-admin-api/v1/api-docs special-key"

if [ -f $APP_DIR/target/scala-$SCALA_RUNNER_VERSION/swagger-codegen*.jar ]; then
  scala -Dscala.usejavacp=true -cp $APP_DIR/target/scala-$SCALA_RUNNER_VERSION/swagger-codegen*.jar $ags
else
  ./sbt assembly
  scala -cp $APP_DIR/target/scala-$SCALA_RUNNER_VERSION/swagger-codegen*.jar $ags
fi

