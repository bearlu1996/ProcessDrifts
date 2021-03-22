# ProcessDrifts

Code, data and evaluation results for paper:

Lu, Y., Chen, Q., & Poon, S. (2021). A Robust and Accurate Approach to Detect Process Drifts from Event Streams. arXiv preprint [arXiv:2103.10749](https://arxiv.org/abs/2103.10749).

Our program uses some of the libraries in the "ProDrift2.5" package (https://apromore.org/platform/tools/).

For your convinience, the source code of our implementation is in the "Implementation" folder. You need to make external reference to the "ProDrift2.5" package to compile the code (You can also find the package in the "lib" folder in our full package "ProcessDrift.zip". See details below).

You can download the full package "ProcessDrift.zip" (including all excutable files and evaluation data) of our implementation at:

https://unisydneyedu-my.sharepoint.com/:u:/g/personal/yalu8986_uni_sydney_edu_au/EetOwKwo7GhNnhSyp-W23IcBhATylP5ibx68ZRVQNQzg-w?e=1XzlAw

With the full package, you can replicate our evaluation results in a simple and convinient way. 

**Make sure you have read the "User guide.pdf" file before you run the program!**

The synthetic data can also be downloaded separately at:

https://unisydneyedu-my.sharepoint.com/:u:/g/personal/yalu8986_uni_sydney_edu_au/EQvEpM0MJl5GpBsOYgpG7CYBhEw_8OEpzxU24lvg_wRs1w?e=4Ss6jn


Note: Only events with “complete” lifecycle transition will be processed by the program. If your log does not contain such information, please add the “lifecycle: transition” to the events first.
