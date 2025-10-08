# $1 = author
# $2 = java, js, ts or whatever file extension
# $3 = date since (dd-mm-yyyy)
if [ -z "$1" ]
 then
   echo "No author supplied"
   echo "gitcount.sh author java 2023-09-01"
   exit 1
fi
if [ -z "$2" ]
  then
    echo "No type supplied Ex: java, js, tsx"
    echo "gitcount.sh author java 2023-09-01"
    exit 1
fi
if [ -z "$3" ]
  then
    echo "No date since supplied Ex: 2023-09-21"
    exit 1
fi

#echo "$2"
if [[ "$2" == "java" ]]
  then
     pattern=".*src\/.*(java)$"
#     echo "$pattern is pattern"
  elif [[ "$2" == "js" || "$2" == "jsx" || "$2" == "ts" || "$2" == "tsx" ]]
    then
      pattern=".*src\/.*(js|jsx|ts|tsx)$"
#      echo "Pattern is $pattern"
fi 

#echo "$pattern"

git log --branches --no-merges --numstat --pretty="%H %as" --author="$1" --since="$3" --until="$4" \
| grep -E $pattern | grep -v node_modules \
| awk '{plus+=$1; minus+=$2; total=plus-minus} END {printf("+%d, -%d, =%d\n", plus, minus, total)}'
# | grep -E ".*src\/.*(\\$2)$" # | grep -v node_modules 

