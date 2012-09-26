#!/bin/bash
SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]:-$0}); pwd)
BASE_DIR=$(cd "$SCRIPT_DIR/../../.."; pwd)
WORK_DIR="target/sphinx-tmp"

/bin/bash "$SCRIPT_DIR/setup-jython-env.sh"

pushd "$BASE_DIR" > /dev/null
cd $WORK_DIR
jar cf sphinx.jar -C jython/Lib/site-packages/Sphinx*/ sphinx
jar uf sphinx.jar -C jython/Lib/site-packages/Jinja2*/ jinja2
jar uf sphinx.jar -C jython/Lib/site-packages/docutils*/ docutils
jar uf sphinx.jar -C jython/Lib/site-packages/Pygments*/ pygments
jar uf sphinx.jar -C jython/Lib/site-packages/simplejson*/ simplejson
jar uf sphinx.jar -C jython/Lib/site-packages/rst2pdf*/ rst2pdf
jar uf sphinx.jar -C jython/Lib/site-packages/ reportlab
mv sphinx.jar "$BASE_DIR/src/main/resources/"
popd > /dev/null
