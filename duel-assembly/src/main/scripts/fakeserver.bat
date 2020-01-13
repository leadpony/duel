@echo off
rem Copyright 2019-2020 the original author or authors.
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

set program_dir=%~dp0

pushd %program_dir%..
set parent_dir=%CD%
popd

if not defined DUEL_HOME (
    set DUEL_HOME=%parent_dir%
)

if defined JAVA_HOME (
    set java_cmd=%JAVA_HOME%\bin\java
) else (
    set java_cmd=java
)

set module=org.leadpony.duel.fake.server/org.leadpony.duel.fake.server.ServerCommand

if "%1" == "start" (
    start "Duel Fake Server" "%java_cmd%" ^
        "-Dduel.home=%DUEL_HOME%" ^
        -p "%DUEL_HOME%\server" ^
        -m %module% ^
        %*
) else (
    "%java_cmd%" ^
        "-Dduel.home=%DUEL_HOME%" ^
        -p "%DUEL_HOME%\server" ^
        -m %module% ^
        %*
)
