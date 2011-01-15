Please see the LICENSE.txt file for licensing information.

ItemCounter and ItemDoubleAccumulators are Java objects that are handy
for accumulating counts of sets of items, in memory.  They have
some basic summary statistics, serialization, and pretty-
printing capabilities.

The motivation for using these classes instead of Bag data structures 
available in the Apache Commons collections library and Google Guava 
library is to have a single-purpose API with few dependencies.
I expect most users will copy the classes into their projects and 
add a domain-specific functions as needed.

Enjoy!

Brian Romanowski
romanows@gmail.com
