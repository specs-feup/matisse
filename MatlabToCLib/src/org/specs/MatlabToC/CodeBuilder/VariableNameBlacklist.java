/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabToC.CodeBuilder;

import java.util.Set;

public class VariableNameBlacklist {
    private VariableNameBlacklist() {
    }

    public static void addCKeywords(Set<String> blacklist) {
	// Does not include keywords starting in '_'
	blacklist.add("auto");
	blacklist.add("break");
	blacklist.add("case");
	blacklist.add("char");
	blacklist.add("const");
	blacklist.add("continue");
	blacklist.add("default");
	blacklist.add("do");
	blacklist.add("double");
	blacklist.add("else");
	blacklist.add("enum");
	blacklist.add("extern");
	blacklist.add("float");
	blacklist.add("for");
	blacklist.add("goto");
	blacklist.add("if");
	blacklist.add("inline");
	blacklist.add("int");
	blacklist.add("long");
	blacklist.add("register");
	blacklist.add("restrict");
	blacklist.add("return");
	blacklist.add("short");
	blacklist.add("signed");
	blacklist.add("sizeof");
	blacklist.add("static");
	blacklist.add("struct");
	blacklist.add("typedef");
	blacklist.add("union");
	blacklist.add("unsigned");
	blacklist.add("void");
	blacklist.add("volatile");
	blacklist.add("while");

	blacklist.add("asm");
    }

    public static void addAdditionalCppKeywords(Set<String> blacklist) {
	blacklist.add("alignas");
	blacklist.add("alignof");
	blacklist.add("and");
	blacklist.add("and_eq");
	blacklist.add("bitand");
	blacklist.add("bitor");
	blacklist.add("bool");
	blacklist.add("catch");
	blacklist.add("char16_t");
	blacklist.add("char32_t");
	blacklist.add("class");
	blacklist.add("compl");
	blacklist.add("constexpr");
	blacklist.add("const_cast");
	blacklist.add("decltype");
	blacklist.add("delete");
	blacklist.add("dynamic_cast");
	blacklist.add("explicit");
	blacklist.add("export");
	blacklist.add("false");
	blacklist.add("friend");
	blacklist.add("mutable");
	blacklist.add("namespace");
	blacklist.add("new");
	blacklist.add("noexcept");
	blacklist.add("not");
	blacklist.add("not_eq");
	blacklist.add("nullptr");
	blacklist.add("operator");
	blacklist.add("or");
	blacklist.add("or_eq");
	blacklist.add("private");
	blacklist.add("protected");
	blacklist.add("public");
	blacklist.add("reinterpret_cast");
	blacklist.add("static_assert");
	blacklist.add("static_cast");
	blacklist.add("template");
	blacklist.add("this");
	blacklist.add("thread_local");
	blacklist.add("throw");
	blacklist.add("true");
	blacklist.add("try");
	blacklist.add("typeid");
	blacklist.add("typename");
	blacklist.add("using");
	blacklist.add("virtual");
	blacklist.add("wchar_t");
	blacklist.add("xor");
	blacklist.add("xor_eq");
    }

    public static void addLibraryNames(Set<String> blacklist) {
	// stdio.h standard streams
	blacklist.add("stdin");
	blacklist.add("stdout");
	blacklist.add("stderr");

	// stdio.h types
	blacklist.add("FILE");
	blacklist.add("fpos_t");
	blacklist.add("size_t");

	// stdio.h macros
	blacklist.add("BUFSIZ");
	blacklist.add("EOF");
	blacklist.add("FILENAME_MAX");
	blacklist.add("FOPEN_MAX");
	blacklist.add("L_tmpnam");
	blacklist.add("NULL");
	blacklist.add("TMP_MAX");

	// stdio.h functions
	blacklist.add("remove");
	blacklist.add("rename");
	blacklist.add("tmpfile");
	blacklist.add("tmpnam");
	blacklist.add("fclose");
	blacklist.add("fflush");
	blacklist.add("fopen");
	blacklist.add("freopen");
	blacklist.add("setbuf");
	blacklist.add("setvbuf");
	blacklist.add("fprintf");
	blacklist.add("fscanf");
	blacklist.add("printf");
	blacklist.add("scanf");
	blacklist.add("snprintf");
	blacklist.add("sprintf");
	blacklist.add("sscanf");
	blacklist.add("vfprintf");
	blacklist.add("vfscanf");
	blacklist.add("vprintf");
	blacklist.add("vscanf");
	blacklist.add("vsnprintf");
	blacklist.add("vsprintf");
	blacklist.add("vsprintf");
	blacklist.add("vsscanf");
	blacklist.add("fgetc");
	blacklist.add("fgets");
	blacklist.add("fputc");
	blacklist.add("fputs");
	blacklist.add("getc");
	blacklist.add("getchar");
	blacklist.add("gets");
	blacklist.add("putc");
	blacklist.add("putchar");
	blacklist.add("puts");
	blacklist.add("ungetc");
	blacklist.add("fread");
	blacklist.add("fwrite");
	blacklist.add("fgetpos");
	blacklist.add("fseek");
	blacklist.add("fsetpos");
	blacklist.add("ftell");
	blacklist.add("rewind");
	blacklist.add("clearerr");
	blacklist.add("feof");
	blacklist.add("ferror");
	blacklist.add("perror");

	// stdlib.h types
	blacklist.add("div_t");
	blacklist.add("ldiv_t");
	blacklist.add("lldiv_t");

	// stdlib.h macros
	blacklist.add("EXIT_FAILURE");
	blacklist.add("EXIT_SUCCESS");
	blacklist.add("MB_CUR_MAX");
	blacklist.add("RAND_MAX");

	// stdlib.h functions
	blacklist.add("atof");
	blacklist.add("atoi");
	blacklist.add("atol");
	blacklist.add("atoll");
	blacklist.add("strtod");
	blacklist.add("strtof");
	blacklist.add("strtol");
	blacklist.add("strtold");
	blacklist.add("strtoll");
	blacklist.add("strtoul");
	blacklist.add("strtoull");
	blacklist.add("rand");
	blacklist.add("srand");
	blacklist.add("calloc");
	blacklist.add("free");
	blacklist.add("malloc");
	blacklist.add("realloc");
	blacklist.add("abort");
	blacklist.add("atexit");
	blacklist.add("at_quick_exit");
	blacklist.add("exit");
	blacklist.add("getenv");
	blacklist.add("quick_exit");
	blacklist.add("system");
	blacklist.add("bsearch");
	blacklist.add("qsort");
	blacklist.add("abs");
	blacklist.add("div");
	blacklist.add("labs");
	blacklist.add("ldiv");
	blacklist.add("llabs");
	blacklist.add("lldiv");
	blacklist.add("mblen");
	blacklist.add("mbtowc");
	blacklist.add("wctomb");
	blacklist.add("mbstowcs");
	blacklist.add("wcstombs");

	// string.h functions
	blacklist.add("memcpy");
	blacklist.add("memmove");
	blacklist.add("strcpy");
	blacklist.add("strncpy");
	blacklist.add("strcat");
	blacklist.add("strncat");
	blacklist.add("memcmp");
	blacklist.add("strcmp");
	blacklist.add("strcoll");
	blacklist.add("strncmp");
	blacklist.add("strxfrm");
	blacklist.add("memchr");
	blacklist.add("strchr");
	blacklist.add("strcspn");
	blacklist.add("strpbrk");
	blacklist.add("strrchr");
	blacklist.add("strspn");
	blacklist.add("strstr");
	blacklist.add("strtok");
	blacklist.add("memset");
	blacklist.add("strerror");
	blacklist.add("strlen");

	// math.h types
	blacklist.add("double_t");
	blacklist.add("float_t");

	// math.h macros
	blacklist.add("math_errhandling");
	blacklist.add("INFINITY");
	blacklist.add("NAN");
	blacklist.add("HUGE_VAL");
	blacklist.add("HUGE_VALF");
	blacklist.add("HUGE_VALL");

	// math.h functions
	blacklist.add("cos");
	blacklist.add("sin");
	blacklist.add("tan");
	blacklist.add("acos");
	blacklist.add("asin");
	blacklist.add("atan");
	blacklist.add("atan2");
	blacklist.add("cosh");
	blacklist.add("sinh");
	blacklist.add("tanh");
	blacklist.add("acosh");
	blacklist.add("asinh");
	blacklist.add("atanh");
	blacklist.add("exp");
	blacklist.add("frexp");
	blacklist.add("ldexp");
	blacklist.add("log");
	blacklist.add("log10");
	blacklist.add("modf");
	blacklist.add("exp2");
	blacklist.add("expm1");
	blacklist.add("ilogb");
	blacklist.add("log1p");
	blacklist.add("log2");
	blacklist.add("logb");
	blacklist.add("scalbn");
	blacklist.add("scalbln");
	blacklist.add("pow");
	blacklist.add("sqrt");
	blacklist.add("cbrt");
	blacklist.add("hypot");
	blacklist.add("erf");
	blacklist.add("erfc");
	blacklist.add("tgamma");
	blacklist.add("lgamma");
	blacklist.add("ceil");
	blacklist.add("round");
	blacklist.add("fmod");
	blacklist.add("trunc");
	blacklist.add("round");
	blacklist.add("lround");
	blacklist.add("llround");
	blacklist.add("rint");
	blacklist.add("lrint");
	blacklist.add("llrint");
	blacklist.add("nearbyint");
	blacklist.add("remainder");
	blacklist.add("remquo");
	blacklist.add("copysign");
	blacklist.add("nan");
	blacklist.add("nextafter");
	blacklist.add("nexttoward");
	blacklist.add("fdim");
	blacklist.add("fmax");
	blacklist.add("fmin");
	blacklist.add("fabs");
	blacklist.add("abs");
	blacklist.add("fma");

	// C math.h macros, C++ math.h functions
	blacklist.add("fpclassify");
	blacklist.add("isfinite");
	blacklist.add("isinf");
	blacklist.add("isnan");
	blacklist.add("isnormal");
	blacklist.add("signbit");
	blacklist.add("isgreater");
	blacklist.add("isgreaterequal");
	blacklist.add("isless");
	blacklist.add("islessequal");
	blacklist.add("islessgreater");
	blacklist.add("isunordered");

	// stdint.h types
	blacklist.add("u?intmax_t");
	blacklist.add("u?int(_least|_fast)?[0-9]+_t");
	blacklist.add("u?intptr_t");

	// stdint.h macros
	blacklist.add("INTMAX_MIN");
	blacklist.add("INTMAX_MAX");
	blacklist.add("UINTMAX_MAX");
	blacklist.add("INT[0-9]+(_LEAST|_FAST)?_(MIN|MAX)");
	blacklist.add("UINT[0-9]+(_LEAST|_FAST)?_MAX");
	blacklist.add("INTPTR_(MIN|MAX)");
	blacklist.add("UINTPTR_MAX");
	blacklist.add("SIZE_MAX");
	blacklist.add("PTRDIFF_(MIN|MAX)");
	blacklist.add("SIG_ATOMIC_(MIN|MAX)");
	blacklist.add("W(CHAR|INT)_(MIN|MAX)");
	blacklist.add("U?INTMAX_C");
	blacklist.add("U?INT[0-9]+_C");
    }
}
