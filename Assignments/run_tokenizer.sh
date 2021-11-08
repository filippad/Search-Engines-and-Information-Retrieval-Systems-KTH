#!/bin/sh
#java -cp classes ir.TokenTest -f Graduate_Groups.txt -p patterns.txt -rp -cf > tokenized_result.txt
java -cp classes ir.TokenTest -f token_test.txt -p patterns.txt -rp -cf > tokenized_result.txt
