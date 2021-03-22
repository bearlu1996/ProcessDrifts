# ProcessDrifts

Code, data and evaluation results for paper:

**Lu, Y., Chen, Q., & Poon, S. (2021). A Robust and Accurate Approach to Detect Process Drifts from Event Streams. arXiv preprint [arXiv:2103.10749](https://arxiv.org/abs/2103.10749).**

Our program uses some of the libraries in the "ProDrift2.5" package (https://apromore.org/platform/tools/).

For your convenience, the source code of our implementation is in the "Implementation" folder. You need to make external reference to the "ProDrift2.5" package to compile the code (You can also find the package in the "lib" folder in our full package "ProcessDrift.zip". See details below).

You can download the full package "ProcessDrift.zip" (including all executable files and evaluation data) of our implementation at:

https://unisydneyedu-my.sharepoint.com/:u:/g/personal/yalu8986_uni_sydney_edu_au/EetOwKwo7GhNnhSyp-W23IcBhATylP5ibx68ZRVQNQzg-w?e=1XzlAw

With the full package, you can replicate our evaluation results in a simple and convenient way and also bring your own data into our method!

**Make sure you have read the "User guide.pdf" file before you run the program!**

The synthetic data can also be downloaded separately at:

https://unisydneyedu-my.sharepoint.com/:u:/g/personal/yalu8986_uni_sydney_edu_au/EQvEpM0MJl5GpBsOYgpG7CYBhEw_8OEpzxU24lvg_wRs1w?e=4Ss6jn


Note: Only events with “complete” lifecycle transition will be processed by the program. If your log does not contain such information, please add the “lifecycle: transition” to the events first.


The evaluation results can be found in the "Evaluation_results.zip" file. 

For the file names of evaluation results of our algorithm. The number after "w" refers to the window size, and the number after "o" refers to the number of consecutive tests. For example, the results in the folder "add-w1500-o2" refers to the evaluation on the artificial logs with added events as noises. The windowSize is set to 1500, and numOfConsecutiveTests = windowSize / 2.

For the csv files containing evaluation results. The number after n refers to the noise level, the number after R refers to the round of the data (there are 10 rounds for each noise level for each log). For example "R1-IOR-w1500-n0.3-o2.csv" refers to the evaluation results for change pattern "IOR" with noise level 0.3 using the data generated in the first round. (windowsize 1500, numOfConsecutiveTests = windowSize / 2)


The code we use to generate noises in event logs can also be downloaded from:

https://unisydneyedu-my.sharepoint.com/:u:/g/personal/yalu8986_uni_sydney_edu_au/EReEpTVgjuhBs0r-xdM4bh0BkAyYermc01I2dGdDXXU7FQ?e=z62mGv


