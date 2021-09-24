// Single-argument fopen, opens already existent file in read-only mode
// <out> = fopen(<in>)
// Returns -1 if the file could not be open

FILE* f = fopen(in, "r");
if (f == NULL) {
	printf("Could not open file: %s\n", in);
	return -1;
}
int handler = MATISSE_register_file_resource(f);
if (handler < 0) {
	printf("Could not register file handle.\n");
	fclose(f);
}
return handler;
