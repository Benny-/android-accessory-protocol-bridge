## Additional flags to pass to aclocal when it is invoked automatically at
## make time. The ${ACLOCAL_FLAGS} variable is picked up from the environment
## to provide a way for the user to supply additional arguments.
ACLOCAL_AMFLAGS = ${ACLOCAL_FLAGS}

#######################################
# The list of libraries we are building seperated by spaces.
# The 'lib_' indicates that these build products will be installed
# in the $(libdir) directory. For example /usr/lib
lib_LTLIBRARIES = libandroidaccessory-@ANDROIDACCESSORY_API_VERSION@.la

EXTRA_DIST = src/*.h doc/echo.c

# Doxygen (Documentation: man pages, html and more)
if HAVE_DOXYGEN

# For some reason manual pages are not installed. This line does nothing.
dist_libandroidaccessory_@ANDROIDACCESSORY_API_VERSION@_la_MANS = $(top_srcdir)/doc/man/man3/accessory.h.3
$(top_srcdir)/doc/man/man3/accessory.h.3: $(top_srcdir)/doc/doxyfile.stamp

$(top_srcdir)/doc/doxyfile.stamp: $(top_srcdir)/doc/Doxyfile $(top_srcdir)/include/accessory.h $(top_srcdir)/doc/echo.c
	$(DOXYGEN) $(top_srcdir)/doc/Doxyfile
	echo Timestamp > $(top_srcdir)/doc/doxyfile.stamp

CLEANFILES = $(top_srcdir)/doc/doxyfile.stamp

# Installing man pages does not work (see above). doxygen is never invoked.
# We force execution of doxygen here.
all-local: $(top_srcdir)/doc/doxyfile.stamp

clean-local:
	rm -rf $(top_srcdir)/doc/man
	rm -rf $(top_srcdir)/doc/rtf
	rm -rf $(top_srcdir)/doc/latex
	rm -rf $(top_srcdir)/doc/html

endif

# Sources for libandroidaccessory
libandroidaccessory_@ANDROIDACCESSORY_API_VERSION@_la_SOURCES = $(libandroidaccessory_la_HEADERS) \
                        src/accessory.c \
                        src/initialize.c \
                        src/usb.c \
                        src/bt.c

# Headers who will be installed on the system
androidaccessory_includedir = $(includedir)/androidaccessory-$(ANDROIDACCESSORY_API_VERSION)
androidaccessory_include_HEADERS = include/accessory.h

androidaccessory_libincludedir = $(libdir)/androidaccessory-$(ANDROIDACCESSORY_API_VERSION)/include
nodist_androidaccessory_libinclude_HEADERS = androidaccessoryconfig.h

# Compile flags
libandroidaccessory_@ANDROIDACCESSORY_API_VERSION@_la_CFLAGS = -I$(top_srcdir)/include -std=c99

# Linker options
libandroidaccessory_@ANDROIDACCESSORY_API_VERSION@_la_LDFLAGS = -version-info $(ANDROIDACCESSORY_SO_VERSION)

## Install the generated pkg-config file (.pc) into the expected location for
## architecture-dependent package configuration information.  Occasionally,
## pkg-config files are also used for architecture-independent data packages,
## in which case the correct install location would be $(datadir)/pkgconfig.
pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = libandroidaccessory-$(ANDROIDACCESSORY_API_VERSION).pc


