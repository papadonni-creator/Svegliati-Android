package it.svegliati.app
import android.app.*
import android.content.*
import android.os.Build
import org.json.*
import java.util.Calendar

data class Alarm(val id:Int, val h:Int,val m:Int,val days:Int,val enabled:Boolean,val name:String,val green:String,val red:String,val sound:String,val voice:String)
object Store {
 fun prefs(c:Context)=c.getSharedPreferences("svegliati",0)
 fun all(c:Context):List<Alarm>{ val a=JSONArray(prefs(c).getString("alarms","[]")); return (0 until a.length()).map { val o=a.getJSONObject(it); Alarm(o.getInt("id"),o.getInt("h"),o.getInt("m"),o.getInt("days"),o.getBoolean("enabled"),o.getString("name"),o.getString("green"),o.getString("red"),o.getString("sound"),o.getString("voice")) } }
 fun save(c:Context, alarms:List<Alarm>){ val a=JSONArray(); alarms.forEach { a.put(JSONObject().put("id",it.id).put("h",it.h).put("m",it.m).put("days",it.days).put("enabled",it.enabled).put("name",it.name).put("green",it.green).put("red",it.red).put("sound",it.sound).put("voice",it.voice)) }; prefs(c).edit().putString("alarms",a.toString()).apply() }
 fun put(c:Context,a:Alarm){save(c,all(c).filter{it.id!=a.id}+a)}
 fun snooze(c:Context)=prefs(c).getInt("snooze",5)*60000L
}
object Schedule {
 fun pi(c:Context,id:Int,retry:Boolean)=PendingIntent.getBroadcast(c,id,Intent(c,AlarmReceiver::class.java).setAction(if(retry) "retry" else "regular").putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun cancel(c:Context,id:Int,retry:Boolean=false){(c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi(c,id,retry))}
 fun exact(c:Context)=Build.VERSION.SDK_INT<31 || (c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
 fun at(c:Context,id:Int,time:Long,retry:Boolean){ if(!exact(c)) return; val manager=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager; val show=PendingIntent.getActivity(c,id,Intent(c,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE); manager.setAlarmClock(AlarmManager.AlarmClockInfo(time,show),pi(c,id,retry)) }
 fun next(c:Context,a:Alarm){cancel(c,a.id); if(!a.enabled)return; val now=System.currentTimeMillis(); for(i in 0..7){val d=Calendar.getInstance().apply{add(Calendar.DATE,i);set(Calendar.HOUR_OF_DAY,a.h);set(Calendar.MINUTE,a.m);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}; val bit=(d.get(Calendar.DAY_OF_WEEK)+5)%7; if(d.timeInMillis>now && (a.days==0 || a.days and (1 shl bit)!=0)){at(c,a.id,d.timeInMillis,false);return}} }
}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){Store.all(c).forEach{Schedule.next(c,it)}}}
