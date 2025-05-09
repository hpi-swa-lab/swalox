for i in byopl24-*; do 
    cd $i
    echo "Updating $i"
    git pull;
    cd ..
done
