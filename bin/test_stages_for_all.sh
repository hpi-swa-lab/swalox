DIR=`dirname $0`

test() {
    $DIR/$1 ./$2 | tee results/"$2"_tests.txt
}

for i in byopl24-*; do     
    test test_stages.sh $i
done

test test_stages-02.sh  byopl24-02-final