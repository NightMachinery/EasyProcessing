package Alice

/**
 * Created by evar on 8/22/17.
 */

import com.hamoid.VideoExport
import processing.core.PApplet
import processing.core.PFont
import processing.core.PVector
import java.awt.Font
import javax.swing.JOptionPane
import processing.core.PApplet.map as pMap

class A6 : PApplet() {

    lateinit var videoExport: VideoExport
    var recording = false
    lateinit var f: PFont

    override fun settings() {
        size(800, 600)
        f = PFont(Font("Arial", Font.PLAIN, 10), true)
    }

    var locP = PVector(0f, 0f)
    var sizeP = PVector(10000f, 10000f)

    companion object {
        const val SCALE_NOISE = 0.01f
    }

    override fun draw() {
        background(35)
        val sn = SCALE_NOISE
        val gs = 5f
        val gsv = 2f
        textAlign(CENTER)
        for (pastel in pastels) {
            fill(pastel.rgb)
            var loc = PVector(random(width.toFloat()), random(height.toFloat()))
            loc = PVector(pMap(randomGaussian(), -gs, gs, 0f, width.toFloat()), pMap(randomGaussian(), -gsv, gsv, 0f, height.toFloat()))

//            ellipse(loc.x, loc.y, noise(loc.x * sn, loc.y * sn) * 100, noise(loc.x * sn, loc.y * sn) * 100)

            textFont(f,noise(loc.x * sn, loc.y * sn)*30)
//            fill(pastel.darker().rgb)
            text("PROGRESS",loc.x,loc.y)
        }
        if (recording) {
            videoExport.saveFrame()
        }
    }

    override fun setup() {
        background(255)
        noStroke()
        videoExport = VideoExport(this)
        videoExport.startMovie()
    }

    var hasStopped = false;
    override fun keyPressed() {

        if (key == 'r' || key == 'R') {
            recording = !recording
            if (recording and hasStopped)
                videoExport.startMovie()
            PApplet.println("Recording is " + if (recording) "ON" else "OFF")
        }
        if (key == 'q') {
            videoExport.endMovie()
            hasStopped = true
            recording = false
//            exit()
        }
        if (key == 's')
            saveFrame("frame-#####.png")
        if (key == 'n') {
            val fr = JOptionPane.showInputDialog("Specify framerate.").toFloat()
            frameRate(fr)
            videoExport.setFrameRate(fr)
        }

    }
}