/* shape, dims, result */

int length = 1;
int i;

if (*result == NULL) {
	*result = malloc(sizeof(<CELL_STRUCT_NAME>));
	if (*result == NULL) {
		printf("Failed to allocate cell array.");
		abort();
	}
}

(*result)-><CELL_SHAPE> = malloc(sizeof(int) * dims);
if ((*result)-><CELL_SHAPE> == NULL) {
	printf("Failed to allocate cell array.");
	abort();
}
for (i = 0; i < dims; ++i) {
	int dim = shape[i];

	(*result)-><CELL_SHAPE>[i] = dim;
	length *= dim;
}

(*result)-><CELL_LENGTH> = length;
(*result)-><CELL_DATA> = calloc(length, sizeof(<UNDERLYING_DATA_TYPE>));
