#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
java -jar target/personal-finance-manager-1.0.0.jar "$@"
