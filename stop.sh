#!/bin/bash
echo "Stopping all ClaimBridge services..."

for port in 8761 8085 9091 9092 9093 9094 9095; do
  pid=$(netstat -ano | grep ":$port " | grep "LISTENING" | awk '{print $5}')
  if [ -n "$pid" ]; then
    taskkill //F //PID $pid > /dev/null 2>&1
    echo "  Stopped port $port (PID $pid)"
  fi
done

echo "Done."
