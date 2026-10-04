package nl.iwvan.fitride
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
class MainActivity:AppCompatActivity(){
 private lateinit var status:TextView;private lateinit var result:TextView;private lateinit var ftp:EditText;private lateinit var share:Button
 private var folder:Uri?=null;private var prompt:String=""
 private val folderPicker=registerForActivityResult(ActivityResultContracts.OpenDocumentTree()){u->if(u!=null){contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION);folder=u;getPreferences(MODE_PRIVATE).edit().putString("folder",u.toString()).apply();status.text="Opslagmap ingesteld. Kies nu een FIT-bestand."}}
 private val filePicker=registerForActivityResult(ActivityResultContracts.OpenDocument()){u->if(u!=null)process(u)}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);status=findViewById(R.id.status);result=findViewById(R.id.result);ftp=findViewById(R.id.ftp);share=findViewById(R.id.share);folder=getPreferences(MODE_PRIVATE).getString("folder",null)?.let(Uri::parse);findViewById<Button>(R.id.folder).setOnClickListener{folderPicker.launch(null)};findViewById<Button>(R.id.file).setOnClickListener{filePicker.launch(arrayOf("application/octet-stream","*/*"))};share.setOnClickListener{val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,prompt)};startActivity(Intent.createChooser(i,"Analyseer rit met"))}}
 private fun process(uri:Uri){try{status.text="FIT verwerken...";val pts=contentResolver.openInputStream(uri)!!.use{FitReader.read(it)};val s=RideAnalyzer.summarize(pts,ftp.text.toString().toIntOrNull()?:295);val base="${date(s.start)}_rit";folder?.let{saveCsv(it,"$base.csv",pts);saveText(it,"$base.analysis.txt",analysis(s));appendOverview(it,s,base)};prompt="Analyseer deze fietstraining volgens mijn vaste aanpak. Beoordeel decoupling, wanneer die begint, vergelijk met recente ritten en houd rekening met intensiteit. Besteed ook aandacht aan uitrollen en trapvermogen.\n\n"+analysis(s);result.text=analysis(s);share.isEnabled=true;status.text="Klaar."}catch(e:Exception){status.text="Fout bij verwerken";result.text=e.stackTraceToString()}}
 private fun fmt(x:Double?,n:Int=1)=x?.let{String.format(Locale.US,"%.${n}f",it)}?:"-"
 private fun date(t:Long)=Instant.ofEpochSecond(t).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE)
 private fun analysis(s:RideSummary)=buildString{appendLine("Datum: ${date(s.start)}");appendLine("Duur: ${s.durationSec/3600}u ${(s.durationSec%3600)/60}m");appendLine("Afstand: ${fmt(s.distanceKm)} km");appendLine("Gem. vermogen incl. 0 W: ${fmt(s.avgPower)} W");appendLine("Trapvermogen: ${fmt(s.pedalingPower)} W");appendLine("0 W tijdens bewegen: ${fmt(s.zeroPct)}%");appendLine("Gem. HR: ${fmt(s.avgHr)} bpm");appendLine("NP: ${fmt(s.np)} W | IF: ${fmt(s.iff,2)} | TSS: ${fmt(s.tss)}");appendLine("Pw:HR-decoupling na 10 min: ${fmt(s.decoupling)}%");appendLine("20-minutenblokken:");s.blocks.forEach{appendLine("${it.fromMin}-${it.toMin}: ${fmt(it.power)} W / ${fmt(it.hr)} bpm / Pw:HR ${fmt(it.pwHr,3)}")}}
 private fun dir(u:Uri)=DocumentFile.fromTreeUri(this,u)!!
 private fun saveText(u:Uri,name:String,text:String){val f=dir(u).findFile(name)?:dir(u).createFile("text/plain",name)!!;contentResolver.openOutputStream(f.uri,"wt")!!.bufferedWriter().use{it.write(text)}}
 private fun saveCsv(u:Uri,name:String,p:List<RidePoint>){val f=dir(u).findFile(name)?:dir(u).createFile("text/csv",name)!!;contentResolver.openOutputStream(f.uri,"wt")!!.bufferedWriter().use{w->w.appendLine("timestamp,heart_rate,cadence,distance_m,speed_mps,power,temp");p.forEach{x->w.appendLine("${x.t},${x.hr?:""},${x.cadence?:""},${x.distanceM?:""},${x.speedMps?:""},${x.power?:""},${x.temp?:""}")}}}
 private fun appendOverview(u:Uri,s:RideSummary,name:String){val d=dir(u);val f=d.findFile("ritten_overzicht.csv")?:d.createFile("text/csv","ritten_overzicht.csv")!!;val old=runCatching{contentResolver.openInputStream(f.uri)?.bufferedReader()?.readText()}.getOrNull().orEmpty();val header="date,name,duration_s,distance_km,avg_power,pedaling_power,zero_pct,avg_hr,np,if,tss,decoupling_pct\n";val row="${date(s.start)},$name,${s.durationSec},${fmt(s.distanceKm)},${fmt(s.avgPower)},${fmt(s.pedalingPower)},${fmt(s.zeroPct)},${fmt(s.avgHr)},${fmt(s.np)},${fmt(s.iff,2)},${fmt(s.tss)},${fmt(s.decoupling)}\n";contentResolver.openOutputStream(f.uri,"wt")!!.bufferedWriter().use{it.write((if(old.isBlank())header else old)+row)}}
}