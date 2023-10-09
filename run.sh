#!/bin/sh
# Path of .fmi file
GRAPH="/home/felix/germany.fmi"

# Longitude
LON=9.098

# Latitude
LAT=48.746 

# Path of .que file
QUE="/home/felix/germany.que"

# Source Node ID
S=638394

java -cp out -Xmx8g MapServer -graph $GRAPH -lon $LON -lat $LAT -que $QUE -s $S