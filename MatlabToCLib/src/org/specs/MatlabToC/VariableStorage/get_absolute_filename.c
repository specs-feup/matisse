//char* get_absolute_filename(const char* filename) {

char* absolute_filename;
char* file_path = NULL;

#ifdef _WIN32
   HMODULE module = GetModuleHandleA(NULL);
   size_t size = 32;
   
   size_t index;
   
   
   do {
      size *= 2;
      file_path = realloc(file_path, size);
   } while (GetModuleFileNameA(module, file_path, size) >= size);
   
   // file_path has the path including the executable name now.
   
   index = strlen(file_path) - 2;
   while (file_path[index] != '\\' && file_path[index] != '/') {
      if (--index <= 0) {
         fprintf(stderr, "Could not get executable path");
         exit(1);
      }
   }
   
   file_path[index + 1] = '\0';
#elif __linux
   size_t size = 32;
   for (;;) {
      size *= 2;
      file_path = realloc(file_path, size);

      ssize_t read_length = readlink("/proc/self/exe", file_path, size);
      if (read_length < 0) {
         fprintf(stderr, "Failed to read path\n");
         abort();
      }
      if (read_length < size) {
         file_path[read_length] = '\0';
         break;
      }
   }

   size_t index = strlen(file_path) - 2;
   while (file_path[index] != '/') {
      if (--index <= 0) {
         fprintf(stderr, "Could not get executable path");
         exit(1);
      }
   }

   file_path[index + 1] = '\0';
#else
// disabled warning: #warning Fallback absolute path - returning relative path
   file_path = malloc(3);
   strcpy(file_path, "./");
#endif

   if (file_path == NULL) {
      fprintf(stderr, "Could not get executable folder\n");
      exit(1);
   }
   
   absolute_filename = malloc(strlen(file_path) + strlen(filename) + 1);
   if (absolute_filename == NULL) {
      fprintf(stderr, "Could not allocate memory for absolute filename\n");
      exit(1);
   }
   strcpy(absolute_filename, file_path);
   strcat(absolute_filename, filename);
   free(file_path);

   return absolute_filename;