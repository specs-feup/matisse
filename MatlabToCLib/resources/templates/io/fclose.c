// fclose(<res>)

MATISSE_initialize_file_resources();

if (res >= 0 && res < 3) {
	printf("Can't close standard file handle.\n");
	abort();
}

FILE* handle = NULL;
if (res >= 0 && res < MATISSE_file_num_available_resources) {
	handle = MATISSE_file_resources[res];
}

if (handle == NULL) {
	printf("Invalid file handle.\n");
	abort();
}

fclose(handle);
MATISSE_file_resources[res] = NULL;
