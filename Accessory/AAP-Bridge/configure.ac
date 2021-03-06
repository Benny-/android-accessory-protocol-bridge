#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ([2.68])
AC_INIT([AAP-Bridge],[1.0],[benny@gmx.it])
AC_CONFIG_SRCDIR([src/main.c])
AC_CONFIG_HEADERS([config.h])

AM_INIT_AUTOMAKE([subdir-objects])

AC_CONFIG_MACRO_DIR([m4])

# Checks for programs.
AM_PROG_CC_C_O

# Pandoc is used for the documentation
AC_CHECK_PROGS([PANDOC], [pandoc])
if test -z "$PANDOC";
   then AC_MSG_WARN([Pandoc not found - continuing without Pandoc support. No man pages output])
fi
AM_CONDITIONAL([HAVE_PANDOC],[test -n "$PANDOC"])

# Checks for packages.
PKG_CHECK_MODULES(DEPENDENCIES, [dbus-1 >= 1.6.1, libandroidaccessory-1.0, libconfig, libpulse])

# Checks for header files.
AC_CHECK_HEADERS([inttypes.h limits.h stddef.h stdint.h stdlib.h string.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_INLINE
AC_TYPE_SIZE_T
AC_TYPE_UINT8_T
AC_CHECK_TYPES([ptrdiff_t])

# Checks for library functions.
AC_FUNC_ERROR_AT_LINE
AC_FUNC_MALLOC
AC_CHECK_FUNCS([memmove memset])

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
