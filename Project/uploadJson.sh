#!/bin/sh

i=1
for file in /home/karla/Search_Engines_DD2476/Project/TestJson/*
do
  curl -XPUT -H "Content-Type: application/json; charset = UTF-8" -d @"${file}" http://localhost:9200/test/_doc/$i?pretty
i=$((i+1))
done
