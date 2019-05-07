#### Task: Add a README in which you briefly describe the issues you faced and how you resolved them.

We followed this instruction (https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c) and did not run into major problems. On the first try we had a small issue with step 4 "Fixing Gradle Sync Errors", but eventually we found the right place to change the versions and then it worked.

#### Task: Add a README in which you briefly describe how your app determines the correct size for the red circle.

Instead of relying on a haarcascade that detects faces, we used a classifier that detects noses right away.

To determine the size (width) of the red circle we used half of the width of the detected nose rectangle.