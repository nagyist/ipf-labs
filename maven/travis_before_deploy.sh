#!/bin/bash
#set -euo pipefail

# Use in .travis.yml
# before_deploy: wget -O - https://raw.githubusercontent.com/oehf/ipf-labs/master/maven/before_deploy.sh --no-check-certificate | sh
#
#  - provider: s3
#    secret_access_key:
#      secure: $SECRET_ACCESS_KEY
#    access_key_id:
#      secure: $ACCESS_KEY_ID
#    bucket: "builds.openehealth.org"
#    skip-cleanup: true
#    local-dir: s3-upload
#    upload-dir: ipf
#    detect_encoding: true
#    on:
#      all_branches: true

release=""

# TRAVIS_TAG: If the current build for a tag, this includes the tag’s name
echo "Checking for TRAVIS_TAG"
if [ ! -z "$TRAVIS_TAG" ]; then
    release=$TRAVIS_TAG
fi

# TRAVIS_BRANCH: For builds not triggered by a pull request this is the name of the branch currently being built; 
# whereas for builds triggered by a pull request this is the name of the branch targeted by the pull request 
# (in many cases this will be master).
# TRAVIS_COMMIT: The commit that the current build is testing
if [ -z "$release" ] && [ ! -z "$TRAVIS_BRANCH" ]; then
	echo "Checking for TRAVIS_BRANCH"
    # escaped_branch="${TRAVIS_BRANCH//\//-}"
    release="${TRAVIS_BRANCH}-${TRAVIS_COMMIT}"
fi

if [ -z "$release" ]; then
    echo "Could not determine branch or tag."
    exit 1
fi

# The absolute path to the directory where the repository being built has been copied on the worker.
cd $TRAVIS_BUILD_DIR

# TRAVIS_REPO_SLUG: The slug (in form: owner_name/repo_name) of the repository currently being built. (for example, “oehf/ipf”).
repo=`basename ${TRAVIS_REPO_SLUG}`

mkdir -p s3-upload/
echo "creating zip file s3-upload/${repo}-${release}.zip from ${TRAVIS_BUILD_DIR}"
zip -q -r s3-upload/$repo-$release.zip . -x *.git*
echo "zip for s3 created"