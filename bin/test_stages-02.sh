# The stages test depend on full lox functionality... e.g. functions and built-in functions
# So we might convert them further to only make them depend on functionality that is already implemented in that stage
echo "Running tests for $1"
for i in examples-02/stages/*lox; do 
    echo $i;
    $1 $i; 
done