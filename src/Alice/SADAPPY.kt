package Alice

import com.hamoid.VideoExport
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.ScriptObjectMirror
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import java.awt.Color
import java.awt.Font
import java.io.*
import javax.script.ScriptEngineManager
import javax.swing.JOptionPane
import processing.core.PApplet.map as pMap

var hColors: List<Color> = ArrayList()
    get() {
        if (field.isEmpty()) {
            val file = File("hColors.txt");

            if (file.isFile and file.canRead()) {
                ObjectInputStream(FileInputStream(file)).use {

                    field = it.readObject() as List<Color>
                }

            } else {
                field = genColors(20, "happyP")
                ObjectOutputStream(FileOutputStream("hColors.txt")).use {
                    it.writeObject(field)
                }
            }
        }
        return field
    }
var dColors: List<Color> = ArrayList()
    get() {
        if (field.isEmpty()) {
            val file = File("dColors.txt");

            if (file.isFile and file.canRead()) {
                ObjectInputStream(FileInputStream(file)).use {

                    field = it.readObject() as List<Color>
                }

            } else {
                field = genColors(20, "sadP")
                ObjectOutputStream(FileOutputStream("dColors.txt")).use {
                    it.writeObject(field)
                }
            }
        }
        return field
    }
var pastels: List<Color> = ArrayList()
    get() {
        if (field.isEmpty()) {
            val file = File("colors.txt");

            if (file.isFile and file.canRead()) {
                ObjectInputStream(FileInputStream(file)).use {

                    field = it.readObject() as List<Color>
                }

            } else {
                field = genColors(50, "colorGenerator")
                ObjectOutputStream(FileOutputStream("colors.txt")).use {
                    it.writeObject(field)
                }
            }
        }
        return field
    }

fun genColors(colorNum: Int = 10, palette: String = "colorGenerator"): List<Color> {
    val engine: NashornScriptEngine = ScriptEngineManager().getEngineByName("nashorn") as NashornScriptEngine
//    engine.lo
    engine.eval(FileReader("colorGen.js"))
    val cols: ScriptObjectMirror = engine.invokeFunction(palette, colorNum) as ScriptObjectMirror
    val nc = ArrayList<Color>()
    @Suppress("LoopToCallChain")
    for (i in 0 until colorNum) {
        val c1: ScriptObjectMirror = cols.getMember("$i") as ScriptObjectMirror
        val c2 = c1.getMember("_rgb") as ScriptObjectMirror
        val c = Color((c2.getMember("0") as Number).toInt(), (c2.getMember("1") as Number).toInt(), (c2.getMember("2") as Number).toInt())
        nc += c
    }
    return nc
}

fun main(args: Array<String>) {
    PApplet.main(SADAPPY::class.java.canonicalName)
}

class SADAPPY : PApplet() {

    lateinit var videoExport: VideoExport
    var recording = false
        set(value) {
            field = value
            PApplet.println("Recording is " + if (field) "ON" else "OFF")
        }
    lateinit var f: PFont

    override fun settings() {
        size(300, 300)
        f = PFont(Font("Arial", Font.PLAIN, 300), true)
    }


    var happy = 0.0
    var sad = 0.0
    override fun draw() {
//        background(35)
        val rg = randomGaussian()
        background(dColors[random(dColors.size.toFloat()).toInt()].rgb)
        val d = 1.05
        var t = if (-d < rg && rg < d) {
            fill(Color.lightGray.rgb)
            background(dColors[random(dColors.size.toFloat()).toInt()].rgb)
            sad++
            ":("
        } else {
            fill(Color.white.rgb)
//            background(hColors[random(hColors.size.toFloat()).toInt()].rgb)
            happy++
            ":)"
        }
        textFont(f)
        text(t, (width / 2).toFloat(), (height / 2).toFloat() - textAscent() * 0.22.toFloat())
        if (recording) {
            videoExport.saveFrame()
        }
    }

    override fun setup() {
        background(255)
        textAlign(PConstants.CENTER, PConstants.CENTER)
        videoExport = VideoExport(this)
        frameRate(10.toFloat())
        videoExport.startMovie()
    }

    var hasStopped = false
    override fun keyPressed() {
        when(key.toLowerCase()){
            'r'->{
                recording = !recording
                if (recording and hasStopped)
                    videoExport.startMovie()
            }
            'q'->{
                videoExport.endMovie()
                hasStopped = true
                recording = false
            }
            's'->{
                saveFrame() //"SADAPPY_frame-#.png"
            }
            'f'->{
                val fr = JOptionPane.showInputDialog("Specify framerate.").toFloat()
                frameRate(fr)
            }
            'i'->{
                kotlin.io.println("FR: $frameRate")
                println("Happy: $happy")
                println("Sad: $sad")
                kotlin.io.println("Ratio: ${sad / happy}")
            }
        }
    }

    override fun frameRate(fps: Float) {
        super.frameRate(fps)
        videoExport.setFrameRate(fps)
    }
}
