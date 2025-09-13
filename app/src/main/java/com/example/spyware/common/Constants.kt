package com.example.spyware.common

class Constants {
    companion object {
        public var timerSeconds:Long = 5*60;
        public var logShift:Int = 4;
        public val pi=3.14159265359;
        public val INTENT_CODE:Int=143535534
        public val outputFileName:String="locationHeatMap.txt"
        public val DISTINGUISHING_DISTANCE_KM:Double=0.1
        public val ERROR="error"
        public val INFO="info"
        public val MESSAGE="message"
        public val WARNING="warning"
        public val LONGITUDE="longitude"
        public val LATITUDE="latitude"
        public val TIMESTAMP="timestamp"
        public val START_TIMESTAMP="startTimestamp"
        public val END_TIMESTAMP="endTimestamp"
    }
}