#!/bin/bash
javac -cp "json-simple-1.1.1.jar:opencsv-5.9.jar:commons-lang3-3.17.0.jar" -d bin src/*.java
java -cp "bin:json-simple-1.1.1.jar:opencsv-5.9.jar:commons-lang3-3.17.0.jar" Main src/data/infos.json
