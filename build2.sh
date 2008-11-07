#! /bin/sh
# $Id$
#

# +-------------------------------------------------------------------------+
# | Verify and Set Required Environment Variables                           |
# +-------------------------------------------------------------------------+
if [ "$JAVA_HOME" = "" ] ; then
	echo "***************************************************************"
	echo "  ERROR: JAVA_HOME environment variable not found."
	echo ""
	echo "  Please set JAVA_HOME to the Java JDK installation directory."
	echo "***************************************************************"
	exit 1
fi

#
# build.sh always calls the version of ant distributed with ArgoUML
#

if [ -e `pwd`/../argouml/src/argouml-core-tools ] ; then
	ANT_HOME=`pwd`/../argouml/src/argouml-core-tools/apache-ant-1.7.0
elif [ -e `pwd`/../argouml/tools ] ; then
	ANT_HOME=`pwd`/../argouml/tools/apache-ant-1.7.0
else
        echo "***************************************************************"
	echo "  ERROR: tools directory not found."
	echo ""
	echo "  Please check-out the argouml tools directory to"
	echo ""
	echo " `pwd`/../../tools"
	echo "    or"
	echo " `pwd`/../argouml-core-tools"
	echo "***************************************************************"
	exit 1
fi

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant -Doverride.build.properties=../argouml/src/argouml-app/src/eclipse-ant-build.properties -Dargo.root.dir=../argouml/src $*

#exit
