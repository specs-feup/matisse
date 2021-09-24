// Ensure resources are initialized (i.e., buffers allocated, stdin/stdout/stderr placed there)
if (MATISSE_file_resources == NULL) {
	MATISSE_file_num_available_resources = 32;
	MATISSE_file_resources = (FILE**) calloc(MATISSE_file_num_available_resources, sizeof(FILE*));

	MATISSE_file_resources[0] = stdin;
	MATISSE_file_resources[1] = stdout;
	MATISSE_file_resources[2] = stderr;
}
