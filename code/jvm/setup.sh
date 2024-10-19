#!/bin/bash

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
	if ! systemctl is-active --quiet docker; then
		echo "Docker is not running. Starting Docker..."
		sudo systemctl start docker
		echo "Docker is now running."
	else
		echo "Docker is already running."
	fi
elif [[ "$OSTYPE" == "darwin"* ]] then
	if !(pgrep -f "Docker.app" > /dev/null); then
		echo "Docker is not running. Starting Docker..."
		open -a Docker
		sleep 10 # Wait for docker to start
		echo "Docker is now running."
	else
		echo "Docker is already running."
	fi
else
	echo "This script is only intended for Linux and macOS systems."
fi
