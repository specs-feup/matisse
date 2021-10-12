function C = main()

    % Initialization
    for i=1:numel(A)
        A(i) = i;
    end

    for i=1:numel(B)
        B(i) = i+1;
    end

    C = mult(A, B);

end

function C = mult(A, B)

    C = A*B;

end
