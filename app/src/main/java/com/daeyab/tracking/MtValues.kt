package com.daeyab.tracking

import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Utmk
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import kotlin.math.sin

object MtValues {
    val MtFacility= mutableListOf<Facility>()
    val MtRoute= mutableListOf<Route>()
    var MT_NAME=""
    var MT_MID=""
    var MT_LOCATION=""
    var MT_LAT=37.552544
    var MT_LNG=127.078014
    var CUR_LAT=37.552544
    var CUR_LNG=127.078014
    var MT_HEIGHT="(-)"
    val locationlog= mutableListOf<LatLng>()

    var HOME_LAT="37.54075"
    var HOME_LNG="127.079333"

    fun getDistance(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double {
        fun deg2rad(deg:Double):Double=(deg * Math.PI / 180.0)
        fun rad2deg(rad:Double):Double=(rad * 180 / Math.PI)
        val theta = lon1 - lon2;
        var dist = sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }


}