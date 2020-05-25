package com.example.arlocationjava;

class Pininfo {
    double pinlatitude;
    double pinlontitude;
    double distance;
    double degree;

    boolean pinplaced;

    Pininfo(double latitude, double lontitude){
        pinlatitude=latitude;
        pinlontitude=lontitude;
        distance=0;
        pinplaced=false;
    }
}
