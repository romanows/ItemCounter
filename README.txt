Please see the LICENSE.txt file for licensing information.

ItemCounter and ItemDoubleAccumulator are Java objects that are handy
for accumulating counts of sets of items, in memory.  They have
some basic summary statistics, serialization, and pretty-
printing capabilities.

The motivation for using these classes instead of Bag data structures 
available in the Apache Commons collections library and Google Guava 
library is to have a single-purpose counting API with no dependencies
and that makes use of generics.  I expect most users will copy the 
classes into their projects and add domain-specific functions as 
needed.

Enjoy!

Brian Romanowski
romanows@gmail.com
