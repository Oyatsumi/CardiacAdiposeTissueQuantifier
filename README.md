# CardiacAdiposeTissueQuantifier
An autonomous quantifier for the mediastinal and epicardial fats.

<br>

<b>Instructions</b>

The Main class in ct-Segmentation folder (which is a subfolder of folder src) should be the one to be compiled. <br>
All the remaining 3 project folders must be included in the "ct-Segmentation" project.<br>

There is a compiled version of this Main.java (which belongs to the ct-Segmentation project) in the bin folder. <br>

<b>Usage</b>

The program takes 5 arguments, which are:

1) The size of the window to be processed in the images (by experimental analysis we have been use 12, which is equivalent to a 25-pixel window [12*12+1]).<br>
2) How many pixels the program should "jump", the standard would be 1 for no pixel jumping.<br>
3) How many slices the program should "jump", the standard would also be 1.<br>
4) The patients folder. The program takes the path of a folder which contains as many other folders as wanted. These included folders are the folders of each patient, where inside each one there are the Dicom files of the corresponding patient.<br>
5) The path to the database (.arff) for the algorithm to train on or to the already trained model. If you have the .attHeader or .model files keep them on the same folder as of your .arff file.<br>

<br>
<b>Practical Example</b>

So, for instance, let's suppose we want to process the patients from folder 1, located in /home_nfs/erodrigues/p3/, then we should run the following command in terminal or windows console:

java -jar -Xmx15g 
segmentator.jar 12 1 1
'/home_nfs/erodrigues/p3/1/'
'/home_nfs/erodrigues/p/12_5_1_1_1_reduced.arff' 


there are 5 arguments. The -Xmx15g means we are giving 15 gigs of memory to the JVM to work on. The segmentator.jar can be found in the bin folder. The arguments are:<br>

1) 12<br>
2) 1<br>
3) 1<br>
4) '/home_nfs/erodrigues/p3/1/'<br>
5) '/home_nfs/erodrigues/p/12_5_1_1_1_reduced.arff'<br>
<br>

<b>Dataset and Model</b><br>

A dataset for training and models can be found at:<br>
http://www.mediafire.com/file/2gbd9wfc893h6d4/datasets_and_models.zip <br>
Mirror link: https://drive.google.com/file/d/0B8feZmt0ma32ZV9wOUF6RWNKVGc/view?usp=sharing


<br>
<b>Previous work:</b><br>
http://www.sciencedirect.com/science/article/pii/S0169260715002448


<br>
For the ground truth of the patients used on the training, please check:<br>
http://visual.ic.uff.br/en/cardio/ctfat/index.php
