#!/bin/bash
SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]:-$0}); pwd)
BASE_DIR=$(cd "$SCRIPT_DIR/../../.."; pwd)
WORK_DIR="target/sphinx-tmp"
pushd "$BASE_DIR" > /dev/null
rm -rf $WORK_DIR
mkdir -p $WORK_DIR
cd $WORK_DIR
# workaround for http://bugs.jython.org/issue1944
LC_ALL=en_US.UTF-8
LANGUAGE=en_US

# Install Jython
curl -LO "http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.5.3/jython-installer-2.5.3.jar"
java -jar jython-installer-2.5.3.jar -s -d jython -t standard
 
# Install setuptools
curl -O "https://bitbucket.org/pypa/setuptools/raw/bootstrap-py24/ez_setup.py"
./jython/bin/jython ez_setup.py

# Install Sphinx and other packages 
./jython/bin/easy_install --no-deps docutils==0.11 pygments==1.6 jinja2==2.6 sphinx==1.2.2 simplejson==2.6.2 rst2pdf==0.93
./jython/bin/easy_install --no-deps javasphinx==0.9.10 javalang==0.9.5 beautifulsoup==3.2.1 sphinxcontrib-httpdomain==1.2.1
 
# reportlab's default setup does not work under Jython, so let's install it manually
# we'll also patch it along the way
curl -O https://pypi.python.org/packages/source/r/reportlab/reportlab-2.7.tar.gz
tar zxf reportlab-2.7.tar.gz
patch -d reportlab*/src/reportlab -p6 < "$BASE_DIR/src/main/build/reportlab.patch"
pushd reportlab* > /dev/null
rm -rf src/rl_addons
../jython/bin/jython setup.py install
popd > /dev/null
 
# we also need to patch rst2pdf
patch -d jython/Lib/site-packages/rst2pdf* -p6 < "$BASE_DIR/src/main/build/rst2pdf.patch"
rm jython/Lib/site-packages/rst2pdf*/rst2pdf/pdfbuilder\$py.class
./jython/bin/jython -mcompileall jython/Lib/site-packages/rst2pdf*/rst2pdf/

# patch javasphinx
patch -d jython/Lib/site-packages/javasphinx* -p1 < "$BASE_DIR/src/main/build/javasphinx.patch"

# patch Sphinx
patch -d jython/Lib/site-packages/Sphinx* -p1 < "$BASE_DIR/src/main/build/sphinx.patch"

# patch sphinxcontrib
patch -d jython/Lib/site-packages/sphinxcontrib* -p1 < "$BASE_DIR/src/main/build/sphinxcontrib.patch"

popd > /dev/null

