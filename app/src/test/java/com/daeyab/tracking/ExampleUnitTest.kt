package com.daeyab.tracking

import com.naver.maps.geometry.Utmk
import org.junit.Test

import org.junit.Assert.*
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
        val x = 203665.28060000017
        val y = 560548.86290000379

        val factory : CRSFactory = CRSFactory()
        val beforeCrs=factory.createFromName("EPSG:5186")
        val afterCrs=factory.createFromName("EPSG:5179")
        val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)
        val beforeCoord= ProjCoordinate(x,y)
        val afterCoord= ProjCoordinate()
        transform.transform(beforeCoord,afterCoord)
        val after= Utmk(afterCoord.x,afterCoord.y).toLatLng()
        print(after)
    }
}
