#if __STDC_VERSION__ >= 199901L
	// restrict is a keyword
	// No need to define it.
#else
	#if defined(_MSC_VER) && _MSC_VER >= 1400
		#define restrict __restrict
	#elif defined(__GNUC__) && ((__GNUC__ > 3) || (__GNUC__ == 3 && __GNUC_MINOR__ >= 1))
		#define restrict __restrict__
	#else
		// Fallback
		#define restrict
	#endif
#endif