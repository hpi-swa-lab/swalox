halt;

var console = lookup("js", "console");


var jsEval = lookup("js", "eval");

console.log("Hello, world! " + String(jsEval("Math.PI")));


var Array = lookup("js", "Array");



var a = Array(1, 2, 3);
console.log(a);
