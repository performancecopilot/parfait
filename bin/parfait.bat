@echo off
@rem -----------------------------------------------------------------------------------
@rem
@rem Copyright (c) 2016 Red Hat
@rem
@rem This is free software; you can redistribute it and/or modify it
@rem under the terms of the GNU Lesser General Public License as
@rem published by the Free Software Foundation; either version 2.1 of
@rem the License, or (at your option) any later version.
@rem
@rem This software is distributed in the hope that it will be useful,
@rem but WITHOUT ANY WARRANTY; without even the implied warranty of
@rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
@rem Lesser General Public License for more details.
@rem
@rem Simple batch file to start a java program with the parfait-agent
@rem jar injected
@rem
@rem usage: parfait [-n name] [-i interval] [-c cluster] [-s startup] [--] javaargs
@rem 
@rem -n use the string which follows as the program name exported
@rem    via PCP memory mapped value (MMV) metrics.  mmv.<name> in
@rem    the performance metric name space (PMNS).
@rem 
@rem -c use the numeric cluster identifier for PCP/MMV metric IDs
@rem    (default is to build a hash identifier using progam name)
@rem 
@rem -i use numeric interval as the delta upon which JMX values are
@rem    reevaluated for exporting as memory mapped values (1 second
@rem    by default).  Specified in milliseconds.
@rem
@rem -s allow a max startup time in which JMX values are still being
@rem    created, before exporting as memory mapped values (5 seconds
@rem    by default).  Specified in milliseconds.
@rem 
@rem -- optional separator to distinguish trailing arguments
@rem 
@rem javaargs trailing arguments to be supplied to the java command
@rem
@rem The script employs the java command found in the current execution
@rem PATH.  If PARFAIT_JAVA_ARGS is set then this is inserted into the
@rem java command line before the -javaagent argument and before any
@rem arguments in javaargs.
@rem
@rem -----------------------------------------------------------------------------------
if "%OS%" == "Windows_NT" setlocal

if "%~1" == "" goto showUsage

@rem set parfait environment
call "%~dp0\setenv.bat"
if %ERRORLEVEL% == 1 goto exitBatch

set AGENT_PREFIX=-javaagent:%PARFAIT_JAR%
set AGENT_OPTS=

set PARFAIT_NAME=
set PARFAIT_CLUSTER=
set PARFAIT_INTERVAL=
set PARFAIT_STARTUP=

@rem ===================================================================================
@rem start parse args section.
@rem ===================================================================================
:startArgsLoop

set ARG=%~1

@rem if there are no arguments, or not an option.
if     "%ARG%"      == ""  goto endArgsLoop
if not "%ARG:~0,1%" == "-" goto endArgsLoop

@rem parse arg.
if "%ARG%" == "-n" if not "%~2" == "" goto setName
if "%ARG%" == "-c" if not "%~2" == "" goto setCluster
if "%ARG%" == "-i" if not "%~2" == "" goto setInterval
if "%ARG%" == "-s" if not "%~2" == "" goto setStartup
if "%ARG%" == "--"                    goto breakArgsLoop

@rem unrecognised option -- must be start of javaargs
goto endArgsLoop

@rem set or add option label --------------------
:setName
set PARFAIT_NAME=%~2
goto shift2AndToNext

:setCluster
set PARFAIT_CLUSTER=%~2
goto shift2AndToNext

:setInterval
set PARFAIT_INTERVAL=%~2
goto shift2AndToNext

:setStartup
set PARFAIT_STARTUP=%~2
goto shift2AndToNext

:breakArgsLoop
shift
goto endArgsLoop
@rem -------------------------------------------

@rem util label --------------------------------
:shift2AndToNext
shift
shift
goto startArgsLoop
@rem -------------------------------------------

:endArgsLoop
@rem ===================================================================================
@rem end parse args section.
@rem ===================================================================================

@rem ===================================================================================
@rem start trailing arg section --------------------------------------------------------
@rem ===================================================================================
:startTrailingArgsLoop
set ARG=%~1
if "%ARG%" == "" goto endTrailingArgsLoop
set TRAILING_ARGS=%TRAILING_ARGS% %ARG%
shift
goto startTrailingArgsLoop
:endTrailingArgsLoop
@rem ===================================================================================
@rem end trailing arg section ----------------------------------------------------------
@rem ===================================================================================

@rem --------------------------------------------------------------------
set AGENT_ARGUMENT=%AGENT_PREFIX%=%AGENT_OPTS%

if not "%PARFAIT_NAME%" == "" set AGENT_OPTS=%AGENT_OPTS%,name:%PARFAIT_NAME%
if not "%PARFAIT_CLUSTER%" == "" set AGENT_OPTS=%AGENT_OPTS%,cluster:%PARFAIT_CLUSTER%
if not "%PARFAIT_INTERVAL%" == "" set AGENT_OPTS=%AGENT_OPTS%,interval:%PARFAIT_INTERVAL%
if not "%PARFAIT_STARTUP%" == "" set AGENT_OPTS=%AGENT_OPTS%,startup:%PARFAIT_STARTUP%

@rem ===================================================================================

@rem Execute Java Program
java %PARFAIT_JAVA_OPTS% "%AGENT_ARGUMENT%" %TRAILING_ARGS%

goto exitBatch

:exitBatch
if "%OS%" == "Windows_NT" endlocal
exit /b

@rem ---------------------------------------------------------------------------------------
@rem Usage
@rem ---------------------------------------------------------------------------------------
:showUsage
echo usage: parfait [-n name] [-i interval] [-c cluster] [-s startup] [--] javaargs
echo.
echo   -n use the string which follows as the program name exported
echo      via PCP memory mapped value (MMV) metrics.  mmv.[name] in
echo      the performance metric name space (PMNS).
echo.
echo   -c use the numeric cluster identifier for PCP/MMV metric IDs
echo      (default is to build a hash identifier using progam name)
echo.
echo   -i use numeric interval as the delta upon which JMX values are
echo      reevaluated for exporting as memory mapped values (1 second
echo      by default).  Specified in milliseconds.
echo.
echo   -s allow a max startup time in which JMX values are still being
echo      created, before exporting as memory mapped values (5 seconds
echo      by default).  Specified in milliseconds.
echo.
echo   --  optional separator to distinguish trailing arguments
echo.
echo   javaargs  trailing arguments to be supplied to the java command
echo.
echo The script constructs a -javaagent argument to pass to the java
echo command found in the current execution PATH.  If PARFAIT_JAVA_ARGS
echo is set then this is inserted into the java command line before
echo the -javaagent argument and before any arguments in javaargs.
goto exitBatch
