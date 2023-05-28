#!/bin/bash
# this file is uesd to generate the '.yml' file

ls | grep -E ".*\.i" | while read line
do
		printf "format_version: '2.0'\n\\n\n\\ninput_files: '$line'\n\n\\nproperties:\n\\n    - property_file: ../../../unreach-call.prp\n\\n      expected_veridict: true\n\noptions:\n\\n    language: C\n\\n    data_model: ILP32\n" > "${line%.i}.yml"\n
done
