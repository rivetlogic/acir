@echo off
set cp=.;%MULE_HOME%\conf;%MULE_HOME%\lib\opt\groovy-all-1.5.6-osgi.jar;%MULE_HOME%\lib\boot\commons-cli-1.0-osgi.jar
java -Dmule.home=%MULE_HOME% -cp "%cp%" org.codehaus.groovy.tools.GroovyStarter --main groovy.ui.GroovyMain --conf %MULE_HOME%\bin\launcher.conf %*