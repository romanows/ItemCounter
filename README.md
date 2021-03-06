# Overview
ItemCounter is a Java class that stores a count for each appearance  
of an item.  ItemDoubleAccumulator stores the accumulated value  
associated with each appearance of an item.  They both support basic   
summary statistics, serialization, and pretty-printing capabilities.   

An example use of ItemCounter is to count the number of times   
different words appear in a set of documents.  One could then output  
all words used, sorted by their frequency of occurrence.  

Similar functionality is available in the Bag data structures in    
the Apache Commons collections library and Google Guava library.  
ItemCounter classes are designed with a single-purpose API, no   
external dependencies, and sane default behavior.  They also take   
advantage of Java generics.  

[Javadoc](http://romanows.github.com/ItemCounter/doc/index.html) is available online.  Feedback and bugfixes are welcomed.  

Brian Romanowski  
romanows@gmail.com  


# Details
This code is licensed under one of the BSD variants, please see   
LICENSE.txt for full details.  


# Example
    // Print the number of lower-case tokens observed in a string.
		
    String words = "foo bar bat baz foo foo baz";
    ItemCounter<String> wordCount = new ItemCounter<String>();
    for(String word : words.split("\\s+")) {
       wordCount.increment(word.toLowerCase());
    }
    System.out.println(wordCount.toCSV());

