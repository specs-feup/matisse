#include <stdio.h>

#ifdef __linux__
	#define IS_LINUX 1
#else
	#define IS_LINUX 0
#endif

#ifdef _WIN32
	#define IS_WINDOWS 1
#else
	#define IS_WINDOWS 0
#endif

#ifdef __MICROBLAZE__
	#define IS_MICROBLAZE 1
#else
	#define IS_MICROBLAZE 0
#endif

int main() {
	printf("Is Linux?      -> %d\n", IS_LINUX);
	printf("Is Windows?    -> %d\n", IS_WINDOWS);
	printf("Is MicroBlaze? -> %d\n", IS_MICROBLAZE);
}