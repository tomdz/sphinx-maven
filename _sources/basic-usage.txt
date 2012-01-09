.. _`Sphinx`: http://sphinx.pocoo.org/
.. _`Sphinx first steps tutorial`: http://sphinx.pocoo.org/tutorial.html
.. _`conf.py`: http://sphinx.pocoo.org/config.html
.. _`Sphinx' examples page`: http://sphinx.pocoo.org/examples.html
.. _`reStructured Text`: http://docutils.sf.net/rst.html
.. _`Werkzeug`: http://werkzeug.pocoo.org/docs/
.. _`Werkzeug's github page`: https://github.com/mitsuhiko/werkzeug/tree/master/docs
.. _`Celery`: http://docs.celeryproject.org/en/latest/index.html
.. _`Celery's github page`: http://docs.celeryproject.org/en/latest/index.html
.. _`Maven 3 site plugin wiki page`: https://cwiki.apache.org/MAVEN/maven-3x-and-site-plugin.html
.. _`Maven 3 site plugin howto`: http://whatiscomingtomyhead.wordpress.com/2011/06/05/maven-3-site-plugin-how-to/

.. _contents:

Basic Usage
===========

First, create a folder ``src/site/sphinx``. This folder will contain the `reStructured Text`_ source files plus
any additional things like themes and configuration. The name of the folder can be changed via options should
you want a different folder.

Next, add the documentation. The `Sphinx first steps tutorial`_ gives a good introduction into the required
tasks. Basically what you need is

* A configuration file called `conf.py`_ that defines the theme and other options (such as which output formats etc.)
* The documentation files in reStructured Text format.
* Additional files such as static files (images etc.), usually in a ``_static`` sub directory.
* Optionally, a customized theme in a sub directory called ``_theme``

For good examples of documentation, see `Sphinx' examples page`_.  The documentation for this plugin itself is
based on the documentation for `Werkzeug`_ (documentation source for it can be found on `Werkzeug's github page`_)
and `Celery`_ (documentation source can be found on `Celery's github page`_).

Finally, add the sphinx maven plugin to your ``pom.xml``::

    <reporting>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-project-info-reports-plugin</artifactId>
          <version>2.4</version>
          <reportSets>
            <reportSet>
              <reports></reports>
            </reportSet>
          </reportSets>
        </plugin>
        <plugin>
          <groupId>org.tomdz.maven</groupId>
          <artifactId>sphinx-maven-plugin</artifactId>
          <version>1.0.0</version>
        </plugin>
      </plugins>
    </reporting>

It is important that you set the ``reportSet`` attribute of the ``project-info-reports`` plugin to an empty set of
``reports``. If not then the default ``about`` report will be generated which conflicts with the ``sphinx-maven``
plugin, and in effect Sphinx will not be run.

*Maven 3* changes how reporting plugins are specified. A ``profile`` can be used to define a ``pom.xml`` that can
be used with both Maven 2 and Maven 3::

    <profiles>
      <profile>
        <id>maven-3</id>
        <activation>
          <file>
             <!--  This employs that the basedir expression is only recognized by Maven 3.x (see MNG-2363) -->
            <exists>${basedir}</exists>
          </file>
        </activation>
        <build>
          <plugins>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-site-plugin</artifactId>
              <version>3.0</version>
              <configuration>
                <reportPlugins>
                  <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-project-info-reports-plugin</artifactId>
                    <version>2.4</version>
                    <reportSets>
                      <reportSet>
                        <reports></reports>
                      </reportSet>
                    </reportSets>
                  </plugin>
                  <plugin>
                    <groupId>org.tomdz.maven</groupId>
                    <artifactId>sphinx-maven-plugin</artifactId>
                    <version>1.0.0</version>
                  </plugin>
                </reportPlugins>
              </configuration>
            </plugin>
          </plugins>        
        </build>
      </profile>
    </profiles>

The profile will only be activated if Maven 3 is used to generate the site. For more details about Maven 3
and the site plugin see the `Maven 3 site plugin wiki page`_ and this `Maven 3 site plugin howto`_.

Now all you need to do is to generate the documentation::

    mvn site

This will generate the documentation in the `target/site` folder.
