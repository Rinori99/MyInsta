package com.example.asus.myinsta.Utils;

import android.os.Environment;

public class FilePaths {

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String CAMERA = ROOT_DIR + "/DCIM/Camera";
    public String PICTURES = ROOT_DIR + "/Pictures";
    public String SCREENSHOTS = ROOT_DIR + "/DCIM/Screenshots";

    public String FIREBASE_IMAGE_STORAGE = "/photos/users/";
}
