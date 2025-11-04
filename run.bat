@echo off
chcp 65001
title War Game
echo ================================
echo           WAR GAME
echo ================================
echo.

:: Очищаем старую сборку
if exist "build" rmdir /s /q build
mkdir build

echo 🔧 Компиляция игры...
javac -d build -encoding UTF-8 src\*.java

if errorlevel 1 (
    echo ❌ Ошибка компиляции!
    pause
    exit /b 1
)

echo ✅ Компиляция успешна!

:: Копируем текстуры
if not exist "build\textures" mkdir build\textures
if exist "textures\*" (
    xcopy /Y /I textures\* build\textures\ > nul
    echo ✅ Текстуры скопированы
)

echo.
echo 🎮 ЗАПУСК WAR GAME...
echo ================================
java -cp build Main

echo.
echo ================================
echo      Игра завершена
echo ================================
pause