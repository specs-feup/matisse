function timer_test()
    
    foo();

end

function a = foo()
    a = 0;
    
    for i = 0:5
        a = a + bar();
    end
    
end


function result = bar() 
    result = 1.0;
end


