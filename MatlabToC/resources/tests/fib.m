%Fibonacci
function result = fib(n)

if n<=2
 result = 1;
 return;
end;

result = fib(n-2) + fib(n-1);