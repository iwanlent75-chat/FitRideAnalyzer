package nl.iwvan.fitride
import kotlin.math.pow
data class RidePoint(val t:Long,val hr:Int?,val cadence:Int?,val distanceM:Double?,val speedMps:Double?,val power:Int?,val temp:Int?)
data class Block(val fromMin:Int,val toMin:Int,val power:Double?,val hr:Double?,val pwHr:Double?)
data class RideSummary(val start:Long,val durationSec:Long,val distanceKm:Double,val avgPower:Double?,val pedalingPower:Double?,val zeroPct:Double?,val avgHr:Double?,val np:Double?,val iff:Double?,val tss:Double?,val decoupling:Double?,val blocks:List<Block>)
object RideAnalyzer {
 private fun avg(xs:List<Double>)=if(xs.isEmpty())null else xs.average()
 private fun pwHr(ps:List<RidePoint>):Double?{val p=avg(ps.mapNotNull{it.power?.toDouble()});val h=avg(ps.mapNotNull{it.hr?.toDouble()});return if(p!=null&&h!=null&&h>0)p/h else null}
 private fun np(points:List<RidePoint>):Double?{val watts=points.mapNotNull{it.power?.toDouble()};if(watts.size<30)return null;val rolling=watts.windowed(30,1).map{it.average()};return rolling.map{it.pow(4)}.average().pow(.25)}
 fun summarize(p:List<RidePoint>,ftp:Int):RideSummary{
  require(p.size>60);val sorted=p.sortedBy{it.t};val start=sorted.first().t;val end=sorted.last().t
  val moving=sorted.filter{(it.speedMps?:0.0)>0.5};val powers=moving.mapNotNull{it.power?.takeIf{w->w in 0..3000}?.toDouble()};val positive=powers.filter{it>0}
  val avgP=avg(powers);val pedal=avg(positive);val zero=if(powers.isEmpty())null else powers.count{it==0.0}*100.0/powers.size
  val hr=avg(moving.mapNotNull{it.hr?.takeIf{h->h in 20..250}?.toDouble()});val n=np(moving);val iff=if(n!=null&&ftp>0)n/ftp else null
  val duration=(end-start).coerceAtLeast(1);val tss=if(iff!=null)duration*iff*iff/36.0 else null;val d=(sorted.mapNotNull{it.distanceM}.maxOrNull()?:0.0)/1000.0
  val afterWarm=moving.filter{it.t>=start+600};val mid=if(afterWarm.isEmpty())start else(afterWarm.first().t+afterWarm.last().t)/2
  val a=pwHr(afterWarm.filter{it.t<=mid});val b=pwHr(afterWarm.filter{it.t>mid});val dec=if(a!=null&&b!=null&&a>0)(a-b)/a*100 else null
  val blocks=mutableListOf<Block>();var from=10
  while(start+from*60<end){val to=from+20;val q=moving.filter{it.t>=start+from*60&&it.t<start+to*60};if(q.isNotEmpty()){val bp=avg(q.mapNotNull{it.power?.toDouble()});val bh=avg(q.mapNotNull{it.hr?.toDouble()});blocks+=Block(from,to,bp,bh,pwHr(q))};from+=20}
  return RideSummary(start,duration,d,avgP,pedal,zero,hr,n,iff,tss,dec,blocks)
 }
}
