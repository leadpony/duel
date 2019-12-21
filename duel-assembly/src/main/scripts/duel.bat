@echo off
rem Copyright 2019 the original author or authors.
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

setlocal

set programdir=%~dp0

if not defined DUEL_HOME (
    pushd %programdir%..
    set DUEL_HOME=%CD%
    popd
)

if defined JAVA_HOME (
    set javacmd=%JAVA_HOME%\bin\java
) else (
    set javacmd=java
)

"%javacmd%" ^
    %DUEL_OPTIONS% ^
    "-Dduel.home=%DUEL_HOME%" ^
    -p "%DUEL_HOME%\lib;%DUEL_HOME%\plugin" ^
    -m org.leadpony.duel.cli/org.leadpony.duel.cli.DuelCommand ^
    %*
