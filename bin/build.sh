for i in byopl24-*; do 
    cd $i;
    ./mvnw compile || true
    cd ..
done
