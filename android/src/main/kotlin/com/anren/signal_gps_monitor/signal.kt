package com.anren.signal_gps_monitor

import android.content.Context
import android.os.Build
import android.os.Handler
import android.telephony.*
import android.util.Log
import java.util.*


class Signal(context: Context) {
    var Great: Int = 4
    var Good: Int = 3
    var Moderate: Int = 2
    var Poor: Int = 1
    var NoSignal: Int = 0

    var mTelephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    var mIsListening: Boolean = false
    var mStrength: Int = 0
    var mDbm: Int = 0
    var mAsu: Int = 0
    var mOnUpdate: IListener ?= null
    var mTimerHandler: Handler = Handler()
    var mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mIsListening) {
                updateSignalStrength()
            }
            mTimerHandler.postDelayed(this, 1000)
        }
    }

    init {
        mTimerHandler.postDelayed(mRunnable, 1000)
    }

    fun start(listener: IListener) {
        if (mIsListening) return

        this.mOnUpdate = listener
        mIsListening = true
    }

    fun stop() {
        if (!mIsListening) return
        mIsListening = false
    }

    fun isListinging(): Boolean {
        return mIsListening
    }

    fun getStrength(): Map<String, Int> {
        var strength = HashMap<String, Int>()
        strength.put("strength", mStrength)
        strength.put("dbm", mDbm)
        strength.put("dbm", mAsu)
        return strength
    }

    fun updateSignalStrength() {
        // SIM 卡状态
        val state: Int = mTelephonyManager.simState
        if (state != TelephonyManager.SIM_STATE_READY) {
            mDbm = 0
            mAsu = 0
            mStrength = NoSignal

            var strength = HashMap<String, Int>()
            strength.put("strength", mStrength)
            strength.put("dbm", mDbm)
            strength.put("dbm", mAsu)
            mOnUpdate?.onUpdate(strength)
            return
        }

        //获取网络信号强度
        var cellInfoList = mTelephonyManager.allCellInfo
        var dbm = -1
        var asu = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && cellInfoList != null) {
            for (cellInfo in cellInfoList) {
                if (!cellInfo.isRegistered) continue
                if (cellInfo is CellInfoGsm) dbm =cellInfo.cellSignalStrength.dbm
                if (cellInfo is CellInfoCdma) dbm =cellInfo.cellSignalStrength.dbm
                if (cellInfo is CellInfoWcdma) dbm =cellInfo.cellSignalStrength.dbm
                if (cellInfo is CellInfoLte) dbm =cellInfo.cellSignalStrength.dbm
            }
        }
        if (mTelephonyManager.networkType == TelephonyManager.NETWORK_TYPE_LTE) {
            // For Lte SignalStrength: dbm = ASU - 140.
            asu = dbm + 140
        } else {
            // For GSM Signal Strength: dbm =  (2*ASU)-113.
            asu = (dbm + 113)/2
        }
        Log.v("test", "----- asu: $asu dbm: $dbm")
        mDbm = dbm
        mAsu = asu
        if (asu >= 99) {
            mStrength = NoSignal
        } else if (asu >= 42) {
            mStrength = Great
        } else if (asu >= 35) {
            mStrength = Good
        } else if (asu >= 30) {
            mStrength = Moderate
        } else if (asu >= 25) {
            mStrength = Poor
        } else {
            mStrength = NoSignal
        }

        var strength = HashMap<String, Int>()
        strength.put("strength", mStrength)
        strength.put("dbm", mDbm)
        strength.put("dbm", mAsu)
        mOnUpdate?.onUpdate(strength)
    }

    /**
     * 自定义接口
     */
    interface IListener {
        fun onUpdate(signalStrength: Map<String, Int>)
    }
}