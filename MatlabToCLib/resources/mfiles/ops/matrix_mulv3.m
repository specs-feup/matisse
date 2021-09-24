function C = matrix_mulv3(A, B)

nRowA = size(A, 1);
nColA = size(A, 2);
nRowB = size(B, 1);
nColB = size(B, 2);

% Create variable that will be the type of multiplication
% TODO: This test should be done inside the call to class, so that it can be removed in compile time
test = A(1)+B(1);
%shape = [nRowA, nColB];

C = zeros(nRowA, nColB, class(test));

if nColA ~= nRowB
    fprintf('inner dimensions must match. Matrix A is %d by %d, Matrix B is %d by %d\n', nRowA, nColA, nRowB, nColB);
	return;
end

% Loop interchange that maximizes performance
for j = 1:nColB
    for inner = 1:nColA
		for i = 1:nRowA
			C(i,j) = C(i,j) + A(i,inner)*B(inner,j);
         end
     end
end