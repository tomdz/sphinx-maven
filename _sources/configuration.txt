.. _`Sphinx commandline documentation`: http://sphinx.pocoo.org/man/sphinx-build.html?highlight=command%20line
.. _`Sphinx tag documentation`: http://sphinx.pocoo.org/markup/misc.html#tags

.. _contents:

Configuration
=============

The ``sphinx-maven`` plugin has these configuration options:

==================== ================================================================================================= ========================================
Parameter            Description                                                                                       Default value
==================== ================================================================================================= ========================================
``sourceDirectory``  The directory containing the documentation source.                                                ``${basedir}/src/site/sphinx``
``outputDirectory``  The directory where the generated output will be placed.                                          ``${project.reporting.outputDirectory}``
``outputName``       The base name used to create the report's output file(s).                                         ``index``
``name``             The name of the report.                                                                           ``Documentation via sphinx``
``description``      The description of the report.                                                                    ``Documentation via sphinx``
``builder``          The builder to use. See the `Sphinx commandline documentation`_ for a list of possible builders.  ``html``
``verbose``          Whether Sphinx should generate verbose output.                                                    ``true``
``warningsAsErrors`` Whether warnings should be treated as errors.                                                     ``false``
``force``            Whether Sphinx should generate output for all files instead of only the changed ones.             ``false``
``tags``             Additional tags to pass to Sphinx. See the `Sphinx tag documentation`_ for more information.      
==================== ================================================================================================= ========================================
