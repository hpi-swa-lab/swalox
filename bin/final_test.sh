GREEN="\033[0;32m"
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "------------------------------------------------------------------------"
echo -e "FINAL LOX TESTS"
echo -e "------------------------------------------------------------------------"
echo -e ""
for i in examples/lox/*lox; do 
    RESULT=`./lox $i`
    OUT=`echo $i | sed s/lox$/out/`
    EXPECTED=`cat $OUT`
     if [ "$RESULT" = "$EXPECTED" ]; then
        echo -e "${GREEN}- Passed $i${NC}"
    else
        echo -e "${RED}- Failed $i${NC}"
    fi
done
