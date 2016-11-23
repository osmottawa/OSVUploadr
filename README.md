# OSVUploadr

An application that helps you upload photos from action cameras like gopro and garmin virb (for now) to OpenStreetView.

## How to use ##
To start the application you need the java runtime environment(JRE) version 1.8+. To launch it you can double click the *.jar* file. If that doesn't work, running it via the command line via *java -jar [jar file path]/OSVUploadr.jar* will.

![Mainscreen](https://cloud.githubusercontent.com/assets/498547/17795874/01a06620-658a-11e6-9b05-79c5236b1cee.PNG)

1. Click "**Add Folder**" to select the folder you want to upload (sequences must already be split into individual folders)
2. Browse to the desired folder and hit the "**Save**" button ![save](https://cloud.githubusercontent.com/assets/498547/17811191/630c3ff0-65ef-11e6-9b76-8ca7c87479f8.PNG)
3. You will be prompted if you want to add all subfolders of the selected directory. If you click "**Yes**" then all immediate subfolders will be added instead of selecting them one by one. If you click "**No**" then just that folder will be added.![prompt](https://cloud.githubusercontent.com/assets/498547/17811312/10916790-65f0-11e6-995f-78b1650bcc96.PNG)
4. After you have added folders to the queue. Press the "**Upload**" button to start the upload process. ![listofdirs](https://cloud.githubusercontent.com/assets/498547/17811345/3a7fb8ae-65f0-11e6-912c-e5224b0bb523.PNG)

## Change Language ##
The language if currently available in the application will be based on your computer's system's default language. Current languages are translated via transifex, so if you would like it translated you may do so [here](https://www.transifex.com/james2432/osvuploadr)To change the language you may do so by passing the `-Duser.language` as a java parameter.  Example:

    java -jar -Duser.language=es OSVUploadr.jar

    java -jar -Duser.language=en OSVUploadr.jar

    java -jar -Duser.language=fr OSVUploadr.jar

## Remove duplicates ##
Once you have added the folders to be processed, press the "**Remove Duplicates**" button.
This is a **non destructive** option, it will simply move the duplicates into a folder called duplicates. If you ever need them again they will be available. This will process every folder in the directory list.

## Contribute ##
If you want to contribute to the translation of this application you may do so [here](https://www.transifex.com/james2432/osvuploadr)
Currently the application is only available in English, French and Spanish. If you need a language added to the project, just open an issue on this github page.

## Known issues ##
- Upload process is not threaded, so the upload button will look frozen until it finishes the upload.
- If images aren't geotagged the application will probably crash
- Application is only for action cameras(geotagged images with original photo date exif). This includes photos taken for mapillary in jpg format as well as photos you have geotagged yourself. I will eventually incorporate images/videos taken from the APP.
