for i in examples/lox/*lox; do 
    echo "Calibrating $i"
    RESULT=`./lox $i`
    OUT=`echo $i | sed s/lox$/out/`
    ./lox $i > $OUT
done
