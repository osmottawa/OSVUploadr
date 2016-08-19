# OSVUploadr

An application that helps you upload photos from action cameras like gopro and garmin virb (for now) to OpenStreetView.

## How to use ##
To start the application you need the java runtime environment(JRE) version 1.8+. To launch it you can double click the *.jar* file. If that doesn't work, running it via the command line via *java -jar [jar file path]/OSVUploadr.jar* will.

![Mainscreen](https://cloud.githubusercontent.com/assets/498547/17795874/01a06620-658a-11e6-9b05-79c5236b1cee.PNG)

1. Click "**Add Folder**" to select the folder you want to upload (sequences must already be split into individual folders)
2. After you have added folders to the queue. Press the "**Upload**" button to start the upload process. ![Upload](https://cloud.githubusercontent.com/assets/498547/17796000/27775f74-658b-11e6-994f-fe9947123952.PNG)


## Known issues ##
- Upload process is not threaded, so the upload button will look frozen until it finishes the upload.
- If images aren't geotagged the application will probably crash
- Remove Duplicates does nothing (for now)
- Application is only for action cameras. I will eventually incorporate images/videos taken from the APP.
- No resume from last file. Currently reuploads from image 1. (Currently working on this)