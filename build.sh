#!/bin/bash

mvn clean package -Dmaven.test.skip=true

docker build -t dongbin/fit2cloud2.0-generator:1.0.0 .
