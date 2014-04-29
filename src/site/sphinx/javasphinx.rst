``javasphinx``
==============

.. versionadded:: 1.0.4
    Extension ``javasphinx`` is bundled within ``sphinx-maven``.

Introduction
------------

To use this extension, just add the extension to your configuration file::

    extensions = [...,'javasphinx',...]

See http://bronto.github.io/javasphinx/ for the documentation.

Sample
------

.. java:type:: public interface List<E> extends java.util.Collection<E>, java.lang.Iterable<E>

    An ordered collection (also known as a *sequence*)

    :param E: type of item stored by the list
   

.. _generate-apidoc:
   
Goal ``generate-apidoc``
------------------------

.. versionadded:: 1.0.4
    A goal ``generate-apidoc`` is available to extract java doc from Java source files.
   
It is also possible to ask javasphinx to generate the Javadoc from the source code.
The goal ``generate-apidoc`` accepts the following  arguments:

=========================== ================================================================================================= ========================================
Parameter                   Description                                                                                       Default value
=========================== ================================================================================================= ========================================
``sourceDirectory``         The directory containing the Java source.                                                         ``${basedir}/src/main/java``
``outputDirectory``         The directory where the generated output will be placed.                                          ``${project.build.directory}/src/site/sphinx``
``sphinxSourceDirectory``   The directory where Sphinx is uncompressed.                                                       ``${project.build.directory}/sphinx``
``verbose``                 Whether Sphinx should generate verbose output.                                                    ``true``
``force``                   All existing output files will be rewritten.                                                      ``false``
``update``                  Updated source files will have their corresponding output files updated.                          ``true``  
                            Unchanged files will be left alone. Most projects will want to use this option
``no_toc``                  Don't create a table of contents file                                                             ``false``
``suffix``                  File suffix                                                                                       ``rst``
``includes``                Additional input paths to scan.                                                                   Empty list
``jvm``                     The JVM binary to use. If not set, then will use the one used to run the plugin.
``argLine``                 Additional arguments for the forked JVM instance (such as memory options).
``fork``                    Whether to run Javasphinx in a forked JVM instance.                                               ``false``
``forkTimeoutSec``          How long the plugin should wait for the plugin. 0 means wait forever.                             ``0``
=========================== ================================================================================================= ========================================

The following extract from a :file:`pom.xml` demonstrates how to mix a manually edited Sphinx
document with the documents generates with the goal ``generate-apidoc``:

.. code-block:: xml

    <build>
        ...
	    <plugins>
	        ...
		    <plugin>
			    <artifactId>maven-resources-plugin</artifactId>
			    <version>2.6</version>
			    <executions>
				    <execution>
					    <id>copy-resources</id>
					    <phase>pre-site</phase>
					    <goals>
						    <goal>copy-resources</goal>
					    </goals>
					    <configuration>
						    <outputDirectory>${project.build.directory}/src/site/sphinx</outputDirectory>
						    <resources>
							    <resource>
								    <directory>src/site/sphinx</directory>
								    <filtering>false</filtering>
							    </resource>
						    </resources>
						    <includeEmptyDirs>true</includeEmptyDirs>
					    </configuration>
				    </execution>
			    </executions>
		    </plugin>
		    <plugin>
			    <groupId>org.tomdz.maven</groupId>
			    <artifactId>sphinx-maven-plugin</artifactId>
			    <version>1.0.4</version>
			    <executions>
				    <execution>
					    <id>javasphinx-apidoc</id>
					    <phase>pre-site</phase>
					    <goals>
						    <goal>generate-apidoc</goal>
					    </goals>
					    <configuration>
						    <force>true</force>
						    <sourceDirectory>${basedir}/src/main/java</sourceDirectory>
						    <outputDirectory>${project.build.directory}/src/site/sphinx</outputDirectory>
						    <includes>
						        <include>path1</include>
						        <include>path2</include>
						    </includes>
					    </configuration>
				    </execution>
				    <execution>
					    <id>generate</id>
					    <goals>
						    <goal>generate</goal>
					    </goals>
					    <phase>site</phase>
					    <configuration>
						    <sourceDirectory>${project.build.directory}/src/site/sphinx</sourceDirectory>
					    </configuration>
				    </execution>
			    </executions>
		    </plugin>
		    ...
	    </plugins>
	    ...
    </build>

The main index file can reference the generated documentation from the ``toctree`` directive, for example:

.. code-block:: rest

    Index
    =====

    Some introduction...

    .. toctree::
       :maxdepth: 2

       javadoc (generated) <packages>
       

