#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Suppliy the json file as an argument"
    echo "Example ./chean-up-json.sh en.json"
    exit;
fi

file=./$1
file_tmp=${file}_tmp

echo "Inspecting file $file"

for i in `sed -n '/^\s*.[[:upper:]]/p' ${file} | sed -e 's/.*"\(.*\)"\:.*/\1/'`; 
do 
	echo "Removing.... "$i; 
	jq 'del(.'"${i}"')' ${file} > "${file_tmp}"
	mv $file_tmp $file
done