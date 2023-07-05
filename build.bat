@echo off
@REM # 进入bat所在目录
cd "%~dp0"
./mvnw spotless:apply

mvn clean install -DskipTests=true  -Prelease

cd .\dolphinscheduler-api\target\
tar -cvf api-server.tar.gz .\api-server
cd ..\..\dolphinscheduler-master\target\
tar -cvf master-server.tar.gz .\master-server
cd ..\..\dolphinscheduler-worker\target\
tar -cvf worker-server.tar.gz .\worker-server
cd ..\..