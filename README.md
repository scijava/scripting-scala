A Scala backend for ImageJ's scripting infrastructure
-----------------------------------------------------

[ImageJ2](http://developer.imagej.net/) offers a flexible and powerful scripting
framework based on the plugin infrastructure provided by the [SciJava Common
library](https://github.com/scijava/scijava-common). It allows to add support
for scripting languages without changing the ImageJ code, simply by dropping a
```.jar``` file containing appropriately-annotated classes into the classpath.

This project adds a Scala plugin for the scripting framework and is also
intended to serve as an example how to add additional scripting languages.
