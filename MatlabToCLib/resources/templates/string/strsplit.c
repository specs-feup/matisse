// <out> = strsplit(<in>, <pattern_string>)
// Assuming single-output
// Character-row input
// <pattern_string> is a single-character without any special regex semantics.

char pat = pattern_string[0];

// First, we will find all separator characters
int num_separators = 0;
int i;

for (i = 0; i < <IN_LENGTH>; ++i) {
	char ch = <IN_DATA_i>;

	if (ch == pat) {
		++num_separators;
	}
}

int* seps = calloc(num_separators, sizeof(int));
if (seps == NULL) {
	printf("Memory allocation failure.\n");
	abort();
}

int next_sep = 0;
for (i = 0; i < <IN_LENGTH>; ++i) {
	char ch = <IN_DATA_i>;

	if (ch == pat) {
		seps[next_sep++] = i;
	}
}

// Then, we'll exclude the separators

uint8_t* disabled_seps = calloc(num_separators, sizeof(uint8_t));
if (disabled_seps == NULL) {
	printf("Memory allocation failure.\n");
	free(seps);
	abort();
}
int last_first_sep = -1;

int num_enabled_seps = num_separators;

for (i = 0; i < num_separators; ++i) {
	int sep_index = seps[i];

	if (sep_index == 0) {
		// Don't disable first prefix separator
		last_first_sep = 0;
	} else if (sep_index == last_first_sep + 1) {
		// Disable suffix separator if the input string consists solely of separators
		++last_first_sep;
		disabled_seps[i] = 1;
		num_enabled_seps--;
	} else if (i != (<IN_LENGTH>) - 1 && i != 0 && seps[i - 1] + 1 == sep_index) {
		// Skip consecutive separators
		disabled_seps[i] = 1;
		num_enabled_seps--;
	}
}

// Allocate row cell with substrings
int num_chunks = num_enabled_seps + 1;
<ALLOC_OUT>;

int active_separator = 0;
for (i = 0; i < num_chunks; ++i) {
	while (active_separator < num_separators && disabled_seps[active_separator]) {
		active_separator++;
	}
	int start = active_separator == 0 ? 0 : seps[active_separator - 1] + 1;
	int end;
	if (active_separator == num_separators) {
		end = <IN_LENGTH>;
	} else {
		end = seps[active_separator++];
	}

	// Substring starts at <start>, and ends at <end - 1>.
	// <end> contains the separator itself.
	<DECLARE_CHUNK_MATRIX>;
	<ALLOC_CHUNK>;

	int j;
	for (j = start; j < end; ++j) {
		chunk->data[j - start] = <IN_DATA_j>;
	}
	
	(*out)->data[i] = chunk;
}

free(seps);
free(disabled_seps);

return *out;
