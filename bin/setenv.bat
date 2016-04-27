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
@rem Batch file to set PARFAIT_HOME and PARFAIT_JAR in the environment.
@rem
@rem -----------------------------------------------------------------------------------

@rem use PARFAIT_HOME to locate installed parfait release
if not "%PARFAIT_HOME%" == "" goto gotHome
set "CURRENT_DIR=%cd%"
cd %~dp0
cd ..
set "PARFAIT_HOME=%cd%"
cd "%CURRENT_DIR%"

:gotHome
if exist "%PARFAIT_HOME%\lib\parfait.jar" goto okJar
echo Cannot locate parfait agent jar
exit /b 1

:okJar
set "PARFAIT_JAR=%PARFAIT_HOME%\lib\parfait.jar"
