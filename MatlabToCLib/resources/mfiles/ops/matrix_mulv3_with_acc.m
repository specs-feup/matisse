function C = matrix_mulv3_with_acc(A, B)

nRowA = size(A, 1);
nColA = size(A, 2);
nRowB = size(B, 1);
nColB = size(B, 2);

% Create variable that will be the type of multiplication
test = A(1)+B(2);
shape = [nRowA, nColB];

C = matisse_new_array(shape, class(test));

if nColA ~= nRowB
    fprintf('inner dimensions must match. Matrix A is %d by %d, Matrix B is %d by %d\n', nRowA, nColA, nRowB, nColB);
	return;
end

% Init acc, for type
acc = test;

for i = 1:nRowA
     for j = 1:nColB
			acc = 0;
         for inner = 1:nColA
			acc = acc + A(i,inner)*B(inner,j);
         end
		 C(i,j) = acc;
     end
end