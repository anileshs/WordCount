# WordCount
Naive Java Word Count project

This project is a multi-threaded solution to the problem given below.
---

You want to get a word count of words from multiple documents.
Your program will take in an list of file paths from the command line and end with all the words 
in a data structure containing each word and its count across all of the documents. 
Words are separated by one or more white space characters. 
Ignore differences in capitalization and strip off any punctuation at the start or end of a word.
 
For example, if my documents are:
“I like dogs. Dogs are cute.”
“Are these things like the others?”
 
You will have a map containing:
{
“I”: 1,
“like”: 2,
“dogs”: 2,
“are”: 2,
“cute”: 1,
“these”: 1,
“things”: 1,
“the”: 1,
“others”: 1
}
 
Desired implementation:
An ideal implementation would use threads to parallelize the work as much as possible.
