// int out = MATISSE_register_file_resource(void* f)
// globals: FILE** MATISSE_file_resources; int MATISSE_file_num_available_resources;

MATISSE_initialize_file_resources();

// Skip first 3 resources, because they are reserved for system file handles.
for (int i = 3; i < MATISSE_file_num_available_resources; ++i) {
	if (MATISSE_file_resources[i] == NULL) {
		MATISSE_file_resources[i] = (FILE*) f;
		return i;
	}
}

// No more resource slots available.
// FIXME: Allocate more?
return -1;
