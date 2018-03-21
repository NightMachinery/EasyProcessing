package Alice


//import kotlin.*
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import processing.core.PApplet
import processing.core.PApplet.constrain
import processing.core.PConstants
import processing.core.PFont
import processing.core.PVector
import processing.event.KeyEvent
import processing.event.MouseEvent
import shiffman.box2d.Box2DProcessing
import java.awt.Color
import java.awt.Font
import java.io.*
import java.util.*
import javax.swing.JColorChooser
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import processing.core.PApplet.map as pMap


fun main(args: Array<String>) {
    PApplet.main(ZippedLines::class.java.canonicalName)
}

operator fun ColorVector.plus(color: ColorVector): ColorVector {
    return ColorVector(red + color.red, green + color.green, blue + color.blue, alpha + color.alpha)
}

class ZippedLines : ExtendedPApplet() {
    companion object {
        private const val gravity = 2f
        private const val NOISE_SCALE = 0.07f
    }

    private var textContent = "THOUGHT "
        set(value) {
            field = if (value.isNullOrBlank())
                "EMPTY"
            else
                value
        }

    private var scrollFactor = 5
    private val gaussianLimit: Float
        get() {
//            f = PFont(Font("Arial", Font.PLAIN, 450), true)
            if (arbit != 0f)
                return arbit
//            if (gaussianLimitBody.position.x > 30) {
//                gaussianLimitBody.linearVelocity = Vec2(-40f, -40f)
//                b2d.setGravity(-10f, -10f)
//            } else if (gaussianLimitBody.position.x < 0) {
//                gaussianLimitBody.linearVelocity = Vec2(1f, 1f)
//                b2d.setGravity(gravity, gravity)
//            }
//            if (gaussianLimitBody.position.x <= 0.01f)
//                return 0.01f
            return gaussianLimitBody.position.x
        }
    var verticesCount = 30
    var customVertices = LinkedList<PVector>()
    val committedShapes = LinkedList<LinkedList<PVector>>()
    var redoVertices = LinkedList<PVector>()
    private var closedShape = true
    private var hasCCIed = false
    private var startCharIndex = 0


    override fun settings() {
        size(800, 800)
        f = PFont(Font("Arial", Font.PLAIN, 25 /*10*/), true)
    }


    override fun draw() {
        var endThis = false
        if (preciseLoopMode) {
            currentCharIndex = startCharIndex++
        }
        if (preciseLoopMode && (currentCharIndex % textContent.length) <= 0) {
            if (!hasCCIed) {
                hasCCIed = true
                targetCCI--
                if (targetCCI == 0) {
                    endThis = true
                }
            }
        } else
            hasCCIed = false
        if (endThis) {
            println("Target CCI reached!")
            preciseLoopMode = false
            endMovie()
        }
        defaultBackground()
//        fill(Color.white.rgb)
//        stroke(Color.white.rgb)

        val dots = if (customVertices.isEmpty()) LinkedList<PVector>().apply {
            (1..verticesCount).forEach {
                this.add(PVector(pMap(randomGaussian(), -gaussianLimit, gaussianLimit, mouseX.toFloat() - width / 2, mouseX.toFloat() + width / 2), pMap(randomGaussian(), -gaussianLimit, gaussianLimit, mouseY.toFloat() - height / 2, mouseY.toFloat() + height.toFloat() * 0.5f /*TODne*/)))
//            this.add(PVector(pMap(randomGaussian(), -gaussianLimit, gaussianLimit, 0f, width.toFloat()), pMap(randomGaussian(), -gaussianLimit, gaussianLimit, 0f, height.toFloat()*1f /*TODne*/)))
            }
        } else customVertices

        committedShapes.forEach { drawVertices(it) }
        drawVertices(dots)

        super.draw()
        if (recording && preciseLoopMode) {
            saveFrame(saveDir + "preciseLoop/ZL_frame-######.png")

        }
        if (arbit == 0f)
            b2d.step()
    }

    private fun drawVertices(vertices: LinkedList<PVector>) {
        for (i in 0 until (vertices.size - if (closedShape) 0 else 1) step 1) {
            val oColor = if (i % 2 == 0) secondColor else firstColor
            val dColor = if (i % 2 == 0) firstColor else secondColor
            customLine(vertices[i].x, vertices[i].y, vertices[(i + 1) % vertices.size].x, vertices[(i + 1) % vertices.size].y, oColor, dColor)
        }
    }

    private fun customLine(x1: Float, y1: Float, x2: Float, y2: Float, oColor: Color = Color.yellow, dColor: Color = Color.blue) {
        customLine(PVector(x1, y1), PVector(x2, y2), oColor, dColor)
    }

    private var currentCharIndex = 0

    private var preciseLoopMode: Boolean = false
    private val currentChar: Char
        get() {
            if (currentCharIndex == textContent.length)
                currentCharIndex = 0
            return textContent[currentCharIndex++ % textContent.length]
        }

    var dotSize = 14f//7


    private fun customLine(origin: PVector, dest: PVector, oColor: Color = Color.yellow, dColor: Color = Color.blue) {
        pushStyle()
        val dotCount = (origin.dist(dest) / dotSize/*6*/).toInt() + 1
//        val dotDist = origin.dist(dest) / dotCount
        val dotV = PVector((dest.x - origin.x) / dotCount, (dest.y - origin.y) / dotCount)
        val colDist = ColorVector((dColor.red - oColor.red).toFloat() / dotCount, (dColor.green - oColor.green).toFloat() / dotCount, (dColor.blue - oColor.blue).toFloat() / dotCount, (dColor.alpha - oColor.alpha).toFloat() / dotCount)
        var currentColor = ColorVector(oColor)
        noStroke()
        for (i in 0 until dotCount) {
            origin.add(dotV)
            currentColor += colDist
            currentColor.alpha = constrain(pMap(noise(origin.x * NOISE_SCALE, origin.y * NOISE_SCALE, frameCount * 0.5f), 0f, 1f, -180f, 255f), 0f, 255f)
            fill(currentColor.rgb)
//            ellipse(origin.x, origin.y, r, r)
            textFont(f)
            textAlign(PConstants.CENTER, PConstants.CENTER)
            text(currentChar, origin.x, origin.y)
        }
//        if (currentColor.rgb == Color.white.rgb)
//            kotlin.io.println("shit")
        //println(currentColor.toString() + " yellow" + Color.yellow.toString())
        popStyle()
    }

    private val b2d: Box2DProcessing = Box2DProcessing(this)
    private lateinit var gaussianLimitBody: Body


    override fun setup() {
        surface.setResizable(true)
        defaultFrameRate = 12f
        super.setup()
        b2d.createWorld()
        b2d.setGravity(gravity, gravity)
        gaussianLimitBody = b2d.createBody(BodyDef().apply {
            type = BodyType.DYNAMIC
            position = Vec2(0.01f, 0.01f)
        })
    }


    private var arbit: Float = 0f


    var firstColor = Color.red//Color(color(35f))
    var secondColor = Color.blue//Color.white
//    private var shiftPressed: Boolean = false
//
//    override fun keyReleased() {
//        super.keyReleased()
//        when (keyCode) {
//            PConstants.SHIFT ->
//                shiftPressed = false
//
//        }
//    }


    override fun mouseClicked(event: MouseEvent?) {
        super.mouseClicked(event)
        if (sketchMode) {
            switchLooping()
        } else if (event != null && event.button == PConstants.LEFT) {
            saveState()
            customVertices.add(PVector(event.x.toFloat(), event.y.toFloat()))
            invalidateVerticesRedo()
        } else if (event?.button == PConstants.RIGHT) {
            saveState()
            customVertices.removeAll {
                redoVertices.addFirst(it)
                true
            }
        }

    }

    private fun invalidateVerticesRedo() {
        redoVertices.removeAll { true }
    }

    private var targetCCI: Int = 0

    private var sketchMode: Boolean = false

    override fun keyPressed(event: KeyEvent?) {
        super.keyPressed(event)
        // println(event?.keyCode)
        when (event?.keyCode) {
            38 -> {
                //Up
//                saveState()
                //todo undo for vertices movement
                when {
                    event?.isShiftDown -> {
                        committedShapes.forEach {
                            it.replaceAll {
                                PVector(it.x, it.y - scrollFactor)
                            }
                        }
                    }
                }
                customVertices.replaceAll { PVector(it.x, it.y - scrollFactor) }


            }
            40 -> {
                //Down
//                saveState()
                when {
                    event?.isShiftDown -> {
                        committedShapes.forEach {
                            it.replaceAll {
                                PVector(it.x, it.y + scrollFactor)
                            }
                        }
                    }

                }
                customVertices.replaceAll { PVector(it.x, it.y + scrollFactor) }
            }
            37 -> {
                //Left
//                saveState()
                when {
                    event?.isShiftDown -> {
                        committedShapes.forEach {
                            it.replaceAll {
                                PVector(it.x - scrollFactor, it.y)
                            }
                        }
                    }

                }
                customVertices.replaceAll {
                    PVector(it.x - scrollFactor, it.y)
                }

            }
            39 -> {
                //Right
//                saveState()
                when {
                    event?.isShiftDown -> {
                        committedShapes.forEach {
                            it.replaceAll {
                                PVector(it.x + scrollFactor, it.y)
                            }
                        }
                    }

                }
                customVertices.replaceAll {
                    PVector(it.x + scrollFactor, it.y)
                }

            }
        }
        when (key.toLowerCase()) {
            '\b' -> {
                saveState()
                if (customVertices.size >= 1)
                    redoVertices.addLast(customVertices.removeLast())
            }
            '\\' -> {
                saveState()
                if (redoVertices.size >= 1)
                    customVertices.addLast(redoVertices.removeLast())
            }
            ']' -> {
                customVertices.reverse() //todo Redo will work weird after reversion!
            }
            '[' -> {
                if (customVertices.size >= 1) {
                    saveState()
                    committedShapes.add(customVertices)
                    customVertices = LinkedList()
                    invalidateVerticesRedo()
                } else if (committedShapes.size >= 1) {
                    saveState()
                    customVertices = committedShapes.removeLast()
                }
            }
            '-' -> {
                if (committedShapes.size >= 1)
                    committedShapes.removeLast()
            }
            's', 'ß' -> {
                when {
                    event?.isAltDown == true -> {
                        swingInvoke({
                            ObjectOutputStream(FileOutputStream(saveDir + JOptionPane.showInputDialog("Specify the name for saving vertices.") + ".vertices")).use { it.writeObject(customVertices) }
                        })
                    }
                }
            }
            'v' -> {
                saveState()
                swingInvoke {
                    verticesCount = JOptionPane.showInputDialog("Specify the number of vertices.", verticesCount).toInt()
                }
            }
            '=' -> {
                saveState()
                swingInvoke {
                    try {
                        dotSize = JOptionPane.showInputDialog("Specify the size of dots.", dotSize).toFloat()
                    } catch (e: Exception) {

                    }
                }
            }
            'c' -> {
                saveState()
                closedShape = !closedShape
            }
            'a' -> {
                saveState()

                swingInvoke {
                    arbit = JOptionPane.showInputDialog("Specify arbit (0 = no arbit).", arbit).toFloat()
                }
            }
            'i' -> {
                println("arbit: $arbit")
                kotlin.io.println("DotSize: $dotSize")
                println("VerticesCpunt: $verticesCount")
            }
            't' -> {
                saveState()
                swingInvoke {
                    textContent = JOptionPane.showInputDialog("Specify the text.")
                }
            }
            ',' -> {
                saveState()
                swingInvoke {
                    firstColor = JColorChooser.showDialog(null, "First Color", firstColor)
                }
            }
            '.' -> {
                saveState()
                swingInvoke {
                    secondColor = JColorChooser.showDialog(null, "Second Color", secondColor)
                }
            }
            '`' -> {
                sketchMode = !sketchMode
            }
            '1' -> { //DEBBUG
                background(255)
                f = PFont(Font("Bellyfish", Font.PLAIN, 10 /*10*/), true)
                arbit = 70f
                dotSize = 20f
                verticesCount = 3
//                states[1].save(savePath("./Undo/$uuid/AAA.png"))
//                background(states[1])
            }
            '2' -> {
                f = PFont(Font("FFF Galaxy Extended", Font.PLAIN, 30 /*10*/), true)
            }
            'o', 'ø' -> {
                when {
                    event?.isAltDown == true -> {
                        swingInvoke {
                            ObjectInputStream(FileInputStream(JFileChooser().apply {
                                this.currentDirectory = File(sketchPath())
                                this.fileFilter = object : javax.swing.filechooser.FileFilter() {
                                    override fun accept(f: File): Boolean {
                                        return f.extension.isNullOrEmpty() || f.extension == "vertices"
                                    }

                                    override fun getDescription(): String {
                                        return "Vertices File"
                                    }
                                }
                                showOpenDialog(null)
                            }.selectedFile)).use {
                                saveState()
                                customVertices = it.readObject() as LinkedList<PVector>
                            }
                        }
                    }
                }
            }
            '\t' -> { //Not for end-user.
                saveState()
                noLoop()
                endMovie()
                targetCCI = 1
                startCharIndex = 0
                hasCCIed = true
                currentCharIndex = 0
                preciseLoopMode = true
                recording = true
                loop()
            }

        }

    }


}


data class ColorVector(var red: Float, var green: Float, var blue: Float, var alpha: Float) {
    val rgb: Int
        get() = Color(constrain(red.toInt(), 0, 255), constrain(green.toInt(), 0, 255), constrain(blue.toInt(), 0, 255), constrain(alpha.toInt(), 0, 255)).rgb

    constructor(color: Color) : this(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat())
}
    