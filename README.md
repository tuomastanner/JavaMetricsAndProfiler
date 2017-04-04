Source code metrics and static profiler for Java
================================================
Author: Tuomas Tanner

## Description ##

This program can be used to create source code quality metrics and profile Java programs. It was made for a university project in 2007. Probably not useful for serious profiling (as better tools exist for this). An interesting experiment none the less and the created source code tokenizer and parser may be of some future use.

The analyzed metrics are:
* Line count - both physical and logical
* Cyclomatic Complexity for each method
* Weighted Methods per Class (WMC)
* Depth of Inheritance Tree (DIT)
* Number of Children (NOC)

## Instructions ##

To run the program type the following command:
java -cp bin metrics.GetMetrics [lines, structure, object-metrics, profiler] <directory or single file> [debug]

Type the appropriate mode based on what you would like the program to measure. In the case of lines, structure and object-metrics, the results are printed on screen (you can pipe them to a file). If a directory is given as a parameter, the whole directory is processed recursively.

A full static profiler is also included. When the profiler is selected, it creates a special profiled version of the source code. The profiled source code is output to the "src_profiled" folder.

When the profiled source is compiled and run, inserted profiler methods write log data to a profiler_data.csv log file as the program is run. This log file can then be analyzed with the following command:
java -cp bin metrics.ProfilerReport profile profiler_data.csv 

If you have also created the objectmetrics.txt file, you can combine them like so:
java -cp bin metrics.ProfilerReport combined profiler_data.csv objectmetrics.txt

## Example ##

As an example, results from analyzing the analyzer program itself are included in the linecount.txt and metrics_objectmetrics.txt files. Profiled version of the program's source code itself is included in the src_profiled directory. An example of the output of a profiled run is included in the profiler_data.csv file. The final profiler report is in the testreport.txt file.

## Implementation notes ##

This program uses my own custom tokenizer and a stack based parser. These are used for analyzing the object structure of the source code. When creating a profiled version of the source code, the parser processes each source file and inserts profile log commands to each method entry and exit points.
