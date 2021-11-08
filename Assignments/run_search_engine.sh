#!/bin/sh
javac -d classes -encoding UTF-8 ir/*.java
java -cp classes -Xmx1g ir.Engine -d data/davisWiki -l ir20.png -p patterns.txt
