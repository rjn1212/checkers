#!/bin/bash
cd "$(dirname "$0")"
javac -d out src/*.java && java -cp out CheckerBoardViewer
