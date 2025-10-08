#!/bin/bash

# Script to run gitcount.sh for all authors in the repository
# Usage: gitcountalldaterange.sh <start_date> <end_date> <file_type1> [file_type2] [file_type3] ...
# Example: gitcountall.sh 2025-09-10 2025-09-25 java js ts

if [ -z "$1" ]
then
   echo "No start date supplied"
   echo "Usage: gitcountalldaterange.sh <start_date> <end_date> <file_type1> [file_type2] [file_type3] ..."
   echo "Example: gitcountalldaterange.sh 2025-09-10 2025-09-25 java js ts"
   exit 1
fi

if [ -z "$2" ]
then
   echo "No end date supplied"
   echo "Usage: gitcountalldaterange.sh <start_date> <end_date> <file_type1> [file_type2] [file_type3] ..."
   echo "Example: gitcountalldaterange.sh 2025-09-10 2025-09-25 java js ts"
   exit 1
fi

if [ -z "$3" ]
then
   echo "No file types supplied (java, js, ts, tsx, jsx)"
   echo "Usage: gitcountalldaterange.sh <start_date> <end_date> <file_type1> [file_type2] [file_type3] ..."
   echo "Example: gitcountalldaterange.sh 2025-09-10 2025-09-25 java js ts"
   exit 1
fi

start_date="$1"
end_date="$2"
shift  # Remove the first argument (start_date) so $@ contains only file types
shift  # Remove the second argument (end_date) so $@ contains only file types
file_types=("$@")

echo "Running gitcount.sh for all authors with file types: ${file_types[*]}, since: $start_date" until: $end_date
echo "========================================================================"

# Get all unique authors and iterate through them
./git-authors-uniq.sh | while read -r author; do
    if [ -n "$author" ]; then
        echo "Author: $author"
        
        # Loop through each file type for this author
        for file_type in "${file_types[@]}"; do
            echo "  File type: $file_type"
            ./gitcount.sh "$author" "$file_type" "$start_date" "$end_date"
        done
        
        echo "------------------------------------------------------------------------"
    fi
done