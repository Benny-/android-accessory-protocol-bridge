if HAVE_DOXYGEN

doxyfile.stamp: ../include/accessory.h example.c
	$(DOXYGEN) Doxyfile
	echo Timestamp > doxyfile.stamp

CLEANFILES = doxyfile.stamp

all-local: doxyfile.stamp
clean-local:
	rm -rf $(top_srcdir)/doc/man
	rm -rf $(top_srcdir)/doc/rtf
	rm -rf $(top_srcdir)/doc/latex
	rm -rf $(top_srcdir)/doc/html

endif

