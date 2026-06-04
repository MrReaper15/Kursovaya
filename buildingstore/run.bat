@echo off
chcp 65001 > nul
echo.
echo  ╔══════════════════════════════════════╗
echo  ║     СтройМаркет — Запуск сервера      ║
echo  ╚══════════════════════════════════════╝
echo.

:: Проверяем Java (нужен JDK 17+, рекомендуется 17)
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ОШИБКА] Java не найдена! Установите JDK 17.
    pause
    exit /b 1
)
echo [OK] Java найдена

echo.
echo  Запуск приложения (встроенный Tomcat)...
echo  После старта откройте браузер: http://localhost:8080
echo  Для остановки нажмите Ctrl+C
echo.

:: Maven устанавливать не нужно — используется Maven Wrapper (mvnw.cmd)
call mvnw.cmd -q compile exec:exec
pause
