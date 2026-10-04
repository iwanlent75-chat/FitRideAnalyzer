package nl.iwvan.fitride
import com.garmin.fit.*
import java.io.InputStream
object FitReader {
 fun read(input:InputStream):List<RidePoint>{
  val out=mutableListOf<RidePoint>(); val decode=Decode(); val broadcaster=MesgBroadcaster(decode)
  broadcaster.addListener(RecordMesgListener { m ->
   val ts=m.timestamp?.date?.time?.div(1000) ?: return@RecordMesgListener
   fun power():Int? = m.power?.toInt()?.takeIf{it in 0..3000}
   out += RidePoint(ts,m.heartRate?.toInt(),m.cadence?.toInt(),m.distance?.toDouble(),m.enhancedSpeed?.toDouble() ?: m.speed?.toDouble(),power(),m.temperature?.toInt())
  })
  decode.read(input,broadcaster,broadcaster); return out
 }
}
