Lodes Processor
===============

## Prereqs

* gradle

## Build

### In eclipse

     $ gradle eclipse

Then do your eclipse thing.

### For command line usage

     $ gradle fatjar

Then do your command line thing. 

## Run

You'll need to know where the lodes-processor jar actually is. Also you'll need a block-level shapefile and a LODES7 CSV of your area in the data/shapes/ and data/csvs directories respectively.

     $ java -jar path/to/jar.jar -ag data/type_attribute_groups.csv -a data/csvs/ -s data/shapes/ -i jobs -n jobs