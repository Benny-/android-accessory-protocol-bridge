#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ([2.68])
AC_INIT([libandroidaccessory],[1.0],[benny.jacobs@ict.nl])
AC_CONFIG_SRCDIR([src/config.h])
AC_CONFIG_HEADERS([config.h androidaccessoryconfig.h])

AM_INIT_AUTOMAKE([subdir-objects])

AC_CONFIG_MACRO_DIR([m4])

# Checks for programs.
AC_PROG_CC

dnl Initialize Libtool
LT_INIT

dnl this allows us specify individual liking flags for each target
AM_PROG_CC_C_O 

# Checks for header files.
AC_CHECK_HEADERS([locale.h stdlib.h string.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_TYPE_SSIZE_T
AC_TYPE_UINT8_T

# Checks for library functions.
AC_FUNC_ERROR_AT_LINE
AC_FUNC_MALLOC

# Define these substitions here to keep all version information in one place.
# For information on how to properly maintain the library version information,
# refer to the libtool manual, section "Updating library version information":
# http://www.gnu.org/software/libtool/manual/html_node/Updating-version-info.html
AC_SUBST([ANDROIDACCESSORY_SO_VERSION], [1:1:0])
AC_SUBST([ANDROIDACCESSORY_API_VERSION], [1.0])

AC_CONFIG_FILES([Makefile
                 libandroidaccessory-${ANDROIDACCESSORY_API_VERSION}.pc:libandroidaccessory.pc.in])
AC_OUTPUT
