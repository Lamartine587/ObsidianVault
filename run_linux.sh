#!/bin/bash

# OBSIDIAN VAULT - Linux Launcher
# -------------------------------
# Place this file in the project root (next to pom.xml)

# Define Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}[*] Initializing Obsidian Vault Sequence...${NC}"

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo -e "${RED}[!] Error: JDK is not installed or not in PATH.${NC}"
    echo "    Please install the Java Development Kit (JDK 17 or higher)."
    exit 1
fi

# Create a clean output directory for compiled classes
mkdir -p bin

# Compile
# We point javac to the correct path inside src/main/java
echo -e "${CYAN}[*] Compiling Source Code...${NC}"
javac -d bin src/main/java/ObsidianVault.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}[+] Compilation Successful.${NC}"
    echo -e "${CYAN}[*] Launching Module...${NC}"
    sleep 1

    # Run from the bin directory
    java -cp bin ObsidianVault
else
    echo -e "${RED}[!] Compilation Failed. Check source code for errors.${NC}"
fi