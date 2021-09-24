// void MATISSE_fgetl(<res>, &str, &eof)
// output: char matrix, eof integer marker

MATISSE_initialize_file_resources();

if (res < 0 || res >= MATISSE_file_num_available_resources || MATISSE_file_resources[res] == NULL) {
	printf("Invalid file handle %d.\n", res);
	abort();
}

FILE* f = MATISSE_file_resources[res];

// FXIME: Deal with large file lines
char buf[128];
char* out = fgets(buf, 128, f);
size_t len;
if (out == NULL) {
	// EOF
	len = 0;
	*eof = 1;
} else {
	len = strlen(buf);
	*eof = 0;
}

// Remove trailing \n or \r\n
if (len > 0 && buf[len - 1] == '\n') {
	--len;
}
if (len > 0 && buf[len - 1] == '\r') {
	--len;
}

<ALLOC>

if (len > 0) {
	memcpy((*str)->data, buf, len);
}
