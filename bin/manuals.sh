#!/bin/sh
#
# Copyright (c) 2017 Red Hat
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# Generate Parfait man page and html docs from Markdown.
#

which ronn >/dev/null 2>&1
if [ $? -ne 0 ]
then
    echo "Cannot find 'ronn' program on the PATH"
    exit 1
fi
echo "Building"
ronn PARFAIT.1.md
if [ $? -ne 0 ]
then
    echo "Failed to execute 'ronn' on PARFAIT.1.md"
    exit 1
fi

echo
mv PARFAIT.1 man/parfait.1
mv PARFAIT.1.html man/parfait.html

echo "Manuals:"
find man/ -type f
