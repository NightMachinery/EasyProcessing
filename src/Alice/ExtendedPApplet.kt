package Alice


import com.hamoid.VideoExport
import processing.core.PApplet
import processing.core.PFont
import processing.core.PImage
import processing.event.KeyEvent
import java.awt.Font
import java.util.*
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import processing.core.PApplet.map as pMap

fun main(args: Array<String>) {
    PApplet.main(ExtendedPApplet::class.java.canonicalName)
}

open class ExtendedPApplet : PApplet() {
    companion object {
        private const val MAX_STATES_ALLOWED = 256
        private const val DEFAULT_DIR = "- Unorganized/"
    }

    inner class LimitedLinkedList<T> : LinkedList<T> {

        constructor(c: Collection<T>?) : super(c)
        constructor() : super()

        override fun add(index: Int, element: T) {
            super.add(index, element)
            checkCapacity()
        }


        private fun checkCapacity() {
            val remElms = size - MAX_STATES_ALLOWED
            if (remElms > 0) {
                for (i in 1..remElms)
                    removeFirst()
                stateCounter -= remElms
            }
        }
    }

    protected val uuid = UUID.randomUUID()!! //for use with file-based undo system.
    protected var stateCounter = 0
        set(value) {
            field = if (value < 0)
                0
            else
                value
        }
    protected var freshDrawings = 0 //Possible to use frameCount as well, but let's keep things simple.
    lateinit var videoExport: VideoExport
    var recording = false
        set(value) {
            field = value
            if (field && hasStopped) {
                hasStopped = false
                currentExportSessionID = UUID.randomUUID()
                if (!exportFrames)
                    videoExport.startMovie()
            }
            PApplet.println("Recording is " + if (field) "ON" else "OFF")
        }
    protected lateinit var f: PFont
    protected var hasStopped = false
    protected var experimental: Boolean = false

    var defaultFrameRate: Float = 30f
    protected var saveDir: String = DEFAULT_DIR
        set(value) {
            field = if (value.isNullOrBlank())
                ""
            else
                if (!value.endsWith("/"))
                    value + "/"
                else
                    value

        }

    override fun settings() {
        size(800, 600)
        f = PFont(Font("Arial", Font.PLAIN, 10), true)
    }


    private var currentExportSessionID: UUID = UUID.randomUUID()

    override fun draw() {
        if (recording) {
            if (exportFrames) {
                if (frameCount >= 900) //todo critical remove this shit
                    recording = false
                saveFrame(saveDir + "exportedAnimation - $currentExportSessionID/frame-######.png")
            } else
                try {
                    videoExport.saveFrame()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
        }
        if (++freshDrawings >= 60) {
            freshDrawings = 0
            saveState()
        }
    }

    override fun setup() {
        defaultBackground()
        videoExport = VideoExport(this, DEFAULT_DIR + "- vid.mp4")
        frameRate(defaultFrameRate)
        videoExport.setQuality(100, 128)
        videoExport.startMovie()
        recording = true
        saveState()
    }

    private var exportFrames: Boolean = false

    override fun keyPressed(event: KeyEvent?) {
        super.keyPressed(event)
        when (key.toLowerCase()) {
            'w' -> {
                exportFrames = true
                videoExport.endMovie()
            }
            'r' -> {
                recording = !recording
            }
            'q' -> {
                endMovie()
            }
            'd' -> {
                SwingUtilities.invokeAndWait {
                    saveDir = JOptionPane.showInputDialog("Specify the render directory.", DEFAULT_DIR)
                }
            }
            's', 'ß' -> {
                when {
                    event?.isShiftDown == true -> {
                        swingInvoke {
                            var fname = JOptionPane.showInputDialog("Specify the name.")
                            if (!fname.contains('.'))
                                fname += ".png"
                            saveFrame(saveDir + fname)
                        }

                    }
                    event?.modifiers == 0 -> { //Todo Does it work?
                        saveFrame(saveDir + "ZL_frame-######.png")
                    }
                }
            }
            'f' -> {
                saveState()
                swingInvoke {
                    val fr = JOptionPane.showInputDialog("Specify framerate.", frameRate).toFloat()
                    frameRate(fr)
                }
            }
            'i' -> {
                kotlin.io.println("FR: $frameRate")
                println("Experimental: $experimental")
                println("frameCount: $frameCount")
            }
            ' ' -> {
                switchLooping()
            }
            'e' -> {
                saveState()
                if (experimental)
                    redraw()
                else
                    experimental = true

            }
            'b' -> {
                saveState()
                experimental = false
                defaultBackground()
            }
            'z' -> {
                noLoop()
                loadState(-1)
            }
            'y' -> {
                noLoop()
                loadState(1)
            }
        //TODO Font Chooser
        //todo implement a way to save current state so that painting can be off but clearing the background on. (Set background to last state, that is.)
        }
    }

    protected fun swingInvoke(function: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread())
            function()
        else
            SwingUtilities.invokeAndWait(function)
    }

    protected fun switchLooping() {
        saveState()
        if (looping)
            noLoop()
        else
            loop()
    }

    protected fun loadState(relativeIndex: Int) {
        if (relativeIndex < 0) {
            if ((states.size - 1) < stateCounter) {
                saveState(false /*doesn't make a difference anyways*/)//for redoing
                stateCounter--
            }
        } else {
            if ((states.size - 1) < stateCounter + relativeIndex)
                return //Nothing to redo.
        }
        stateCounter += relativeIndex
        states.getOrNull(stateCounter).run {
            if (this != null)
                this@ExtendedPApplet.background(this)
        }
//        loadImage("./Undo/$uuid/sc$stateCounter.png").run {
//            if (this == null)
////                defaultBackground()
//                println("Requested state was not found.")
//            else
//                background(this)
//        }
    }

    protected fun defaultBackground() {
        if (!experimental) {
            background(255f)
//        background(35f)
            // background(Color.yellow.darker().darker().darker().darker().darker().rgb)
//        background(255f)
        }
    }

    protected fun endMovie() {
        videoExport.endMovie()
        hasStopped = true
        recording = false
    }

    override fun frameRate(fps: Float) {
        super.frameRate(fps)
        videoExport.setFrameRate(fps)
    }

    protected var states: LimitedLinkedList<PImage> = LimitedLinkedList()
    protected fun saveState(invalidateTheFuture: Boolean = true): Boolean {
        //saveFrame("./Undo/$uuid/sc${stateCounter++}.png")
        try {
            freshDrawings = 0
            states.getOrNull(stateCounter - 1).run {
                if (this != null) {
                    graphics.apply {
                        if (!loaded)
                            loadPixels()
                        if (Arrays.equals(pixels, this@run.pixels))
                            return false
                    }
                }
            }
            states.getOrNull(stateCounter).run {
                states.remove(this)
            }
            PImage(graphics.getImage()).save(savePath("./Undo/$uuid/sc${stateCounter}-${Calendar.getInstance().timeInMillis}.png")) //todo debugging purposes, also good for creating fast creation videos :))


            states.add(stateCounter++, PImage(graphics.getImage()).clone() as PImage)
            if (invalidateTheFuture)
                states = LimitedLinkedList(states.filterIndexed { index, _ -> index < stateCounter })
            return true
        } catch (e: Exception) {
            //LoadPixels may throw exceptions when resizing.
            e.printStackTrace()
            return false
        }
    }
}
    