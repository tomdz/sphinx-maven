.. _`rst2pdf bug`: https://code.google.com/p/rst2pdf/issues/detail?id=458

Updating Sphinx
===============

The project comes with a bash script which will update the embedded sphinx installation
automatically. Simple invoke it like so::

    ./src/main/build/update-sphinx.sh

How the update script works
---------------------------

1. It sets up a temporary working directory ``target/sphinx-tmp`` and cd's into it.
2. It downloads the Jython installer for version 2.5.2 from Sourceforge.
3. It downloads the ``ez_setup.py`` script which will setup ``easy_install``.
4. It installs Jython in the temporary directory.
5. It installs ``easy_install`` in the temporary directory.
6. It uses ``easy_install`` to install ``docutils``, ``pygments``, ``jinja2``, ``sphinx``, and ``rst2pdf``.
7. ``rst2pdf`` depends on ``ReportLab``, but unfortunatly that won't install
   directly with ``easy_install``. The reason for that is that by default it is
   trying to install native extensions which won't work on Jython. To workaround
   that, the script downloads the ``ReportLab`` distribution directly, unpacks it,
   removes the native extensions (the setup script will be fine with it), and then
   runs the installation manually.
8. Both ``reportlab`` and ``rst2pdf`` have bugs with Jython (see e.g. this `rst2pdf bug`_) which
   we'll patch and then trigger Jython to pre-compile the modules again.
9. Finally, we create the ``sphinx.jar`` out of the installed modules, and move it to
   ``src/main/resources`` (which will cause it to be included as a file in the plugin).

Steps 1-8 are performed by the `setup-jython-env.sh` script which is executed by
the `update-sphinx.sh` script.

Fixing bugs in one of the Python libraries
==========================================

Occasionally there are bugs in one of the python libraries, either plain bugs or bugs when running
under Jython, that need to be fixed for sphinx-maven to work. In this case, you can use the
``setup-jython-env.sh`` script to setup an unpacked, editable sphinx jython environment::

    ./src/main/build/setup-jython-env.sh

This script will create a temporary folder ``target/sphinx-tmp`` into which it installs Jython and all
relevant libraries plus patch them as necessary (as described above).

Now you can simply use that environment directly::

    "$SPHINX_MAVEN_DIR/target/sphinx-tmp/jython/bin/sphinx-build" -a -E -n -b pdf src/site/sphinx target/site/pdf

where ``SPHINX_MAVEN_DIR`` points to where you have checked out the sphinx maven project.

The neat thing with this is that you can now edit the python code directly. The packages are under::

    target/sphinx-tmp/jython/Lib/site-packages

Once you're done, simply create a patch file to an untouched sphinx + dependencies installation
(see below). E.g. for ``rst2pdf`` ::

    diff -dur -x "*.pyc" -x "*.class" -x "requires.txt" \
        /Library/Python/2.5/site-packages/rst2pdf-0.92-py2.5.egg \
        target/sphinx-tmp/jython/Lib/site-packages/rst2pdf-0.92-py2.5.egg \
        > src/main/build/rst2pdf.patch

And for ``reportlab`` ::

    diff -dur -x "*.pyc" -x "*.class" -x "hyphen.mashed" \
        /Library/Python/2.5/site-packages/reportlab-2.5-py2.5-macosx-10.7-x86_64.egg/reportlab \
        target/sphinx-tmp/jython/Lib/site-packages/reportlab \
        > src/main/build/reportlab.patch


Running normal Sphinx
=====================

If you want to compare to the normal sphinx, install it like this::

    easy_install-2.5 sphinx rst2pdf

Note that this uses Python version 2.5 which is what Jython 2.x provides.
Now run Sphinx like so in your project's root directory::

    sphinx-build -v -a -E -n -b html src/site/sphinx target/site
    sphinx-build -a -E -n -b pdf src/site/sphinx target/site/pdf
