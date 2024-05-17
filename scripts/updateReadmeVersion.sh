#!/bin/sh

# Replace the version in README.md with the version from build.gradle.kts
# Usage:
#   ./scripts/updateReadmeVersion.sh

SED="sed"
if ! command -v gsed &> /dev/null
then
  # brew install gnu-sed
  SED="gsed"
fi

VERSION=$(SED -n 's/^version\s*=\s*"\([0-9]\.[0-9]\.[0-9]\)"/\1/p' < build.gradle.kts)

echo "Updating to version $VERSION in README.md..."
SED -i -E 's/earth\.adi:typeid-kotlin:[0-9]+\.[0-9]+\.[0-9]+/earth\.adi:typeid-kotlin:'"${VERSION}"'/' README.md
SED -i -E 's|<version>[0-9]+\.[0-9]+\.[0-9]+|<version>'"${VERSION}"'|' README.md
SED -i -E 's/Version-[0-9]+\.[0-9]+\.[0-9]+-blue/Version-'"${VERSION}"'-blue/' README.md
