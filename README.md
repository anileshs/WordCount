# WordCount
Naive Java Word Count project

## Problem Statement:

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
 
## Desired implementation:
An ideal implementation would use threads to parallelize the work as much as possible. Consider what parts of the problem can be solved asynchronously and structure your solution accordingly.
 
Please include an automated set of tests (including unit tests) that you feel demonstrates the correctness of your solution. 

---

Solution Description:
---

In simple words,

- one reader thread is spawned; and,
- multiple parsing threads are spawned, their count depending on the number of cores on the machine.

Then, the reader keeps reading the file contents, line by line and the parsers process individual lines, word by word.

Finally, the results of individual parsers are merged to get the final word count.

---

This problem is a classic Producer-Consumer problem where producer puts into a buffer and consumer takes from the buffer.

Since reading is off disk - it is a slow process. Assuming a single machine with reasonable number of cores (<= 32), single reader thread should be fine.

The performance can be tuned for specific cases but in general, multiple reader threads, reading from multiple files at the same time will cause the disk's head to jump all over the place. That is undesirable for HDDs since the movement of head is a mechanical process and therefore slow.

Even for SSDs, having multiple reader threads may not be a great idea as the task is consumer heavy - the consumer having to process multiple words in a line.

### Primary classes in the solution

1. **_Main:_** The entry point which also does the orchestration of threads.
2. **_DiskFileReader:_** A Runnable which performs the task of reading the bunch of file paths provided as the input and put individual lines in a blocking collection.
3. **_LineParser:_** A Callable that reads in lines from a blocking collection and performs word count on each line. The results are stored locally, which can be extracted when the thread is done.
