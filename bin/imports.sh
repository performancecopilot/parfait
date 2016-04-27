#!/bin/sh
#
# Copyright (c) 2016 Red Hat
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
# Convert specified files (or recurse directories) and convert
# java imports of com.custardsource.parfait to io.pcp.parfait.
#

old="com.custardsource.parfait"
new="io.pcp.parfait"

convert()
{
    files=`find $@ -type f -print`
    for file in $files
    do
        sum1=`sum $file`
        sed -i \
            -e "s/^import $old/import $new/g" \
            -e "s/^package $old/package $new/g" \
            $file
        sum2=`sum $file`
        if [ "$sum1" != "$sum2" ]
        then
            echo "Updated $file"
            total=`expr $total + 1`
        fi
    done
}

total=0
[ $# -eq 0 ] && convert .
for anything in $@
do
    convert "$anything"
done
echo "$total files converted"
