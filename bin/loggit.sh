for i in byopl24-*; do 
    cd $i
    COMMITS=`git log --oneline | wc -l`
    echo "Commits $i "$COMMITS
    git log --since="1 week ago" --oneline > ../stats/gitlog_stage_$1_"$i"_$COMMITS
    cd ..
done
