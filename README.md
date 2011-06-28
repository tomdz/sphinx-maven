## Introduction <a name="introduction"></a>

The `sphinx-maven` plugin is a [Maven site](http://maven.apache.org/plugins/maven-site-plugin/) plugin that uses
[Sphinx](http://sphinx.pocoo.org/) to generate the main documentation. Sphinx itself is a tool to generate
documentation out of [reStructured Text](http://docutils.sf.net/rst.html) source files.

## Basic Usage <a name="basic-usage"></a>

1.  Create a folder `src/site/sphinx` (this can be changed via options should you want a different folder).
2.  Generate documentation in it. Basically what you need is

    * A configuration file called [conf.py](http://sphinx.pocoo.org/config.html) that defines the theme and other options (such as which output formats etc.)
    * The documentation files in reStructured Text format
    * Additional files such as static files (images etc.), usually in a `_static` sub directory
    * Optionally, a customized theme in a sub directory called `_theme`

    For good examples of documentation, see [Sphinx' examples page](http://sphinx.pocoo.org/examples.html). Personally, I like
    [Werkzeug](http://werkzeug.pocoo.org/docs/) (documentation source is on [github](https://github.com/mitsuhiko/werkzeug/tree/master/docs)) and
    [Celery](http://docs.celeryproject.org/en/latest/index.html) (documentation is also on [github](https://github.com/ask/celery/tree/master/docs)).
3. Add the sphinx maven plugin to your `pom.xml`:

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
				      <version>1.0.0-SNAPSHOT</version>
				    </plugin>
				  </plugins>
				</reporting>

    It is important that you set the `reportSet` attribute of the `project-info-reports` plugin to an empty set of `reports`. If not
    then the default `about` report will be generated which conflicts with the `sphinx-maven` plugin.
4.  Generate the documentation by running

        mvn site

    This will generate the documentation in the `target/site` folder.

## TODOs

* Add a goal that bootstraps the documentation
* Document integration with other reporting plugins