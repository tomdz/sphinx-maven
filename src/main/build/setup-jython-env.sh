#!/bin/bash
SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]:-$0}); pwd)
BASE_DIR=$(cd "$SCRIPT_DIR/../../.."; pwd)
WORK_DIR="target/sphinx-tmp"
pushd "$BASE_DIR" > /dev/null
rm -rf $WORK_DIR
mkdir -p $WORK_DIR
cd $WORK_DIR
curl -LO "http://downloads.sourceforge.net/project/jython/jython/2.5.2/jython_installer-2.5.2.jar"
curl -O "http://peak.telecommunity.com/dist/ez_setup.py"
java -jar jython_installer-2.5.2.jar -s -d jython -t standard
./jython/bin/jython ez_setup.py
./jython/bin/easy_install docutils 
./jython/bin/easy_install pygments
./jython/bin/easy_install jinja2==2.5
./jython/bin/easy_install sphinx==1.1.3
./jython/bin/easy_install rst2pdf

# reportlab's default setup doesn work under Jython, so let's install it manually
# we'll also patch it along the way
curl -O http://pypi.python.org/packages/source/r/reportlab/reportlab-2.5.tar.gz
tar zxf reportlab-2.5.tar.gz
patch -d reportlab*/src/reportlab -p6 < "$BASE_DIR/src/main/build/reportlab.patch"
pushd reportlab* > /dev/null
rm -rf src/rl_addons
../jython/bin/jython setup.py install
popd > /dev/null

# we also need to patch rst2pdf
patch -d jython/Lib/site-packages/rst2pdf* -p6 < "$BASE_DIR/src/main/build/rst2pdf.patch"
rm jython/Lib/site-packages/rst2pdf*/rst2pdf/pdfbuilder\$py.class
./jython/bin/jython -mcompileall jython/Lib/site-packages/rst2pdf*/rst2pdf/
popd > /dev/null
