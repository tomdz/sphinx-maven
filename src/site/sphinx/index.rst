.. _`Maven site plugin`: http://maven.apache.org/plugins/maven-site-plugin/
.. _`Sphinx`: http://sphinx.pocoo.org/
.. _`reStructured Text`: http://docutils.sf.net/rst.html
.. _`Markdown`: http://daringfireball.net/projects/markdown/

Introduction
============

The *sphinx-maven* plugin is a `Maven site plugin`_ that uses `Sphinx`_ to generate the main documentation.
Sphinx itself was origially created by the Python community for the new Python documentation. It uses a
plain text format called `reStructured Text`_ which it then compiles into a variety of documentation formats
such as HTML, LaTeX (for PDF), epub. reStructured Text is similar to `Markdown`_ but - at least via Sphinx -
has better support for multi-page documentation.

The *sphinx-maven* plugin is BSD licensed just as Sphinx itself is.

.. toctree::
   :maxdepth: 2

   basic-usage
   configuration
   development
   httpdomain
   javasphinx
   faq
   javadoc <apidocs/index.html>
   javadoc (generated) <packages>
