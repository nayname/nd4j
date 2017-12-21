#!/bin/bash
set -evx

while true; do echo .; sleep 60; done &

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    MAVEN_PHASE="deploy"
else
    MAVEN_PHASE="install"
fi

if ! git -C $TRAVIS_BUILD_DIR/.. clone https://github.com/deeplearning4j/libnd4j/ --depth=50 --branch=$TRAVIS_BRANCH; then
     git -C $TRAVIS_BUILD_DIR/.. clone https://github.com/deeplearning4j/libnd4j/ --depth=50
fi

docker run -ti -e SONATYPE_USERNAME -e SONATYPE_PASSWORD -v $HOME/.m2:/root/.m2 -v $TRAVIS_BUILD_DIR/..:/build \
    nvidia/cuda:$CUDA-cudnn$CUDNN-devel-centos6 /bin/bash -evxc "\
        yum -y install centos-release-scl-rh epel-release; \
        yum -y install devtoolset-4-toolchain rh-maven33 cmake3 git java-1.8.0-openjdk-devel; \
        source scl_source enable devtoolset-4 rh-maven33 || true; \
        cd /build/libnd4j/; \
        sed -i /cmake_minimum_required/d CMakeLists.txt
        MAKEJ=2 bash buildnativeoperations.sh; \
        MAKEJ=1 bash buildnativeoperations.sh -c cuda -v $CUDA -cc 30; \
        cd /build/nd4j/; \
        source change-cuda-versions.sh $CUDA; \
        source change-scala-versions.sh $SCALA; \
        mvn clean $MAVEN_PHASE -B -U --settings ./ci/settings.xml -Dmaven.test.skip=true -Dlocal.software.repository=sonatype;"

