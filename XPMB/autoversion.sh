#/bin/bash

CODE=`git tag | grep -c ^v`
NAME=`git describe --dirty`
COMMITS=`echo ${NAME} | sed -e 's/v[0-9\.]*//'`

if [ "x${COMMITS}x" = "xx" ] ; then
    VERSION="${NAME}"
else
    BRANCH=" (`git branch | grep "^\*" | sed -e 's/^..//'`)"
    VERSION="${NAME}${BRANCH}"
fi

cat AndroidManifest.template.xml \\
    | sed -e "s/__CODE__/${CODE}/" \\
          -e   "s/__VERSION__/${VERSION}/" > AndroidManifest.temp.xml
rm AndroidManifest.xml
mv AndroidManifest.temp.xml AndroidManifest.xml

exit 0