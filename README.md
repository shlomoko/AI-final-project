# Compilation Instructions

set JAVA_HOME=c:\Program Files\Java\jdk1.7.0_79
set JAVA_LIBS=%JAVA_HOME%\jre\lib\jfxrt.jar;lib\dom4j-1.6.1.jar;lib\google-collections-1.0.jar;lib\gson-2.6.2.jar;lib\javafx-dialogs-0.0.4.jar;lib\javassist-3.20.0-GA.jar;lib\javax.servlet-api-3.1.0.jar;lib\reflections-0.9.5-RC2.jar;lib\slf4j-api-1.7.18.jar;lib\slf4j-nop-1.7.18.jar
"%JAVA_HOME%\bin\javac.exe" -cp "%JAVA_LIBS%;nonogramSolver\src" -d out nonogramSolver\src\solver\gui\MainWindow.java nonogramSolver\src\solver\gui\Console.java
"%JAVA_HOME%\bin\jar" cvf NonogramSolver.jar out/solver

