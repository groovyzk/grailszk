#!/usr/bin/env sh

while [ "$#" -gt 0 ]; do
    case $1 in
        -v|--version)
        version="$2"; [ -z "$version" ] && {
          echo "Specify the version number to choose a file. Available are:"
          echo "$(ls gradle.v*)"
          exit
        }
        shift
        ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

cp -a "gradle.v${version}.properties" gradle.properties
cp -a "gradle/wrapper/gradle-wrapper.v${version}.properties" gradle/wrapper/gradle-wrapper.properties