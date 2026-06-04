#!/usr/bin/env bash
# Запуск СтройМаркета (встроенный Tomcat) на macOS / Linux.
# Требуется JDK 17 и новее. Maven устанавливать НЕ нужно — используется ./mvnw.
set -e
cd "$(dirname "$0")"

# Пытаемся выбрать JDK 17, если он есть (macOS java_home), иначе берём текущий java.
if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "  ╔══════════════════════════════════════╗"
echo "  ║     СтройМаркет — Запуск сервера      ║"
echo "  ╚══════════════════════════════════════╝"
echo "  JDK: $(java -version 2>&1 | head -1)"
echo "  После старта откройте: http://localhost:8080"
echo "  Остановка: Ctrl+C"
echo

exec ./mvnw -q compile exec:exec
