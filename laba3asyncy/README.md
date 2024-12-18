Matrix Multiplication and Parallel Task Comparison
This project demonstrates two parallel programming techniques—Work Stealing and Work Dealing—applied to matrix multiplication and file search tasks. These techniques are implemented using Java's ForkJoinPool to efficiently manage multi-threading.

Project Overview
Matrix Multiplication
This part of the project performs matrix multiplication using both Work Stealing and Work Dealing techniques to compare their performance. It generates two matrices with random integer values within a user-defined range, multiplies them in parallel, and prints the results. Additionally, the execution time for both approaches is measured and compared.

File Search Task Comparison
In the second part, the program performs a search for a user-specified term in file names within a given directory using both Work Stealing and Work Dealing techniques. It measures the execution time for both approaches and compares them to determine which method is faster.

Key Components
MatrixMultiplication:

Generates two random matrices.
Multiplies the matrices using Work Stealing and Work Dealing.
Measures and compares the execution time of both methods.
ParallelTaskComparison:

Performs a parallel search for a term within file names.
Implements both Work Stealing and Work Dealing strategies.
Measures and compares the execution time of both approaches.
Helper Classes:

MultiplyTask: Recursive task for calculating individual matrix elements in Work Stealing.
MultiplyTaskWorkDealing: Recursive task for calculating matrix rows in Work Dealing.
SearchTask: Recursive task for searching for a term in file names in both Work Stealing and Work Dealing.
Features
Work Stealing:
Tasks are distributed across multiple threads, and idle threads can "steal" tasks from other threads when they finish their work, improving load balancing.
Work Dealing:
Tasks are divided into chunks, and each chunk is assigned to a specific thread. This method ensures that all threads have an equal amount of work.
Instructions
Matrix Multiplication:

Enter the dimensions of the two matrices.
Input the minimum and maximum values for generating random numbers.
The program will display the matrices and their product, along with the execution time for each method (Work Stealing and Work Dealing).
File Search:

Enter the directory path to search in.
Specify the word or letter to search for in the file names.
The program will search for files containing the search term and display the count along with the execution time for each method (Work Stealing and Work Dealing).