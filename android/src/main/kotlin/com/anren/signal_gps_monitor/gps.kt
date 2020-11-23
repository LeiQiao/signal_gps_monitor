package com.anren.signal_gps_monitor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener


class Gps(context: Context) {
    var Great: Int = 4
    var Good: Int = 3
    var Poor: Int = 1
    var NoSignal: Int = 0

    private var context: Context = context
    private var mLocationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var PROVIDER = LocationManager.GPS_PROVIDER
    private var REFRESH_TIME = 1000L
    private var METER_POSITION = 0.0f
    private var mListener: IListener? = null
    private var mGpsListener: GpsListener? = GpsListener()
    private var mIsListeningStrength: Boolean = false
    private var mIsListeningLocation: Boolean = false
    var mStrength: HashMap<String, Any> = HashMap<String, Any>()
    var mLocation: HashMap<String, Double> = HashMap<String, Double>()

    private var authGranted = false
    private var activity: Activity? = null
    private var startLocationMonitorAfterGrant = false
    private var startStrengthMonitorAfterGrant = false

    fun grantPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                //请求权限
                try {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } catch(e: Exception) {
                    Log.v("test", "------ grant gps permission error: ${e.toString()}");
                }
            } else {
                authGranted = true
            }
        } else {
            authGranted = true
        }
    }

    fun onAuthGranted(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != 1) return;

        authGranted = (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
        if (!authGranted) return;

        if (startLocationMonitorAfterGrant) startLocationMonitor(activity!!, mListener!!)
        if (startStrengthMonitorAfterGrant) startStrengthMonitor(activity!!, mListener!!)
    }

    /**
     * 定位监听
     */
    fun startLocationMonitor(activity: Activity, listener: IListener) {
        grantPermission(activity)
        if (!authGranted) {
            this.activity = activity
            mListener = listener
            startLocationMonitorAfterGrant = true
            return
        }

        if (mIsListeningLocation) return
        mIsListeningLocation = true

        mListener = listener

        if (mGpsListener == null) {
            mGpsListener = GpsListener()
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            Log.v("test", "----- no permission")
            return
        }
        mLocationManager.requestLocationUpdates(PROVIDER, REFRESH_TIME, METER_POSITION, mGpsListener)
    }

    /**
     * 取消定位监听
     */
    fun stopLocationMonitor() {
        if (!mIsListeningLocation) return
        mIsListeningLocation = false

        if (mGpsListener != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                Log.v("test", "----- no permission")
                return
            }
            //移除定位监听
            mLocationManager.removeUpdates(mGpsListener)
        }
    }

    /**
     * 信号监听
     */
    fun startStrengthMonitor(activity: Activity, listener: IListener) {
        grantPermission(activity)
        if (!authGranted) {
            this.activity = activity
            mListener = listener
            startStrengthMonitorAfterGrant = true
            return
        }

        if (mIsListeningStrength) return
        mIsListeningStrength = true

        mListener = listener

        if (mGpsListener == null) {
            mGpsListener = GpsListener()
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            Log.v("test", "----- no permission")
            return
        }
        mLocationManager.addGpsStatusListener(mGpsListener)
    }

    /**
     * 取消信号监听
     */
    fun stopStrengthMonitor() {
        if (!mIsListeningStrength) return
        mIsListeningStrength = false

        if (mGpsListener != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                Log.v("test", "----- no permission")
                return
            }
            //移除信号监听
            mLocationManager.removeGpsStatusListener(mGpsListener)
        }
    }

    fun getStrength(): Map<String, Any> {
        return mStrength
    }

    fun getLocation(): Map<String, Double> {
        return  mLocation
    }


    inner class GpsListener : LocationListener, GpsStatus.Listener  {
        override fun onGpsStatusChanged(event: Int) {
            if (mListener != null) {
                updateStrength(event)
                mListener!!.onUpdateStrength(mStrength)
            }
        }

        override fun onLocationChanged(location: Location) { //定位改变监听
            if (mListener != null) {
                updateLocation(location)
                mListener!!.onUpdateLocation(mLocation)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { //定位状态监听
        }

        override fun onProviderEnabled(provider: String) { //定位状态可用监听
        }

        override fun onProviderDisabled(provider: String) { //定位状态不可用监听
        }
    }

    /**
     * 获取信噪比
     */
    fun updateStrength(event: Int) {
        when (event) {
            GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                //获取当前状态
                @SuppressLint("MissingPermission") val gpsStatus: GpsStatus = mLocationManager.getGpsStatus(null)
                //获取卫星颗数的默认最大值
                val maxSatellites = gpsStatus.maxSatellites

                //获取所有的卫星
                val iters: Iterator<GpsSatellite> = gpsStatus.satellites.iterator()
                //卫星颗数统计
                var totalCount = 0
                var validCount = 0
                var snr = 0.0
                while (iters.hasNext() && totalCount <= maxSatellites) {
                    val s = iters.next()
                    if (s.usedInFix()) {
                        validCount++
                    }
                    totalCount++
                    //卫星的信噪比
                    snr += s.snr
                }

                mStrength.put("satelliteCount", validCount)
                mStrength.put("snr", snr)

                if (validCount >= 15) {
                    mStrength.put("strength", Great)
                } else if (validCount >= 10) {
                    mStrength.put("strength", Good)
                } else if (validCount >= 1) {
                    mStrength.put("strength", Poor)
                } else {
                    mStrength.put("strength", NoSignal)
                }
                Log.v("test", "----- gps strength: $mStrength");
            }
            else -> {
            }
        }
    }

    fun updateLocation(location: Location) {
        mLocation.put("longitude", location.longitude)
        mLocation.put("latitude", location.longitude)
        mLocation.put("accuracy", location.accuracy.toDouble())
        mLocation.put("altitude", location.altitude)
        mLocation.put("speed", location.speed.toDouble())
        Log.v("test", "----- location: $mLocation");
    }

    /**
     * 自定义接口
     */
    interface IListener {
        fun onUpdateStrength(gpsStrength: Map<String, Any>)
        fun onUpdateLocation(gpsLocation: Map<String, Double>)
    }

}